package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

// TODO: Make this class implement BookWebClient.
@Component
public class BookWebClientImpl implements BookWebClient {

    private final WebClient webClient;

    public BookWebClientImpl(WebClient.Builder builder, ExternalServiceProperties properties) {
        this.webClient = builder
                .baseUrl(properties.baseUrl())
                // .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        return webClient.get()
                .uri("/books/{id}", id)
                .retrieve()
                .bodyToMono(BookApiResponse.class)
                .timeout(Duration.ofSeconds(3))
                .retry(3)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ClientException("Empty response for book " + id)))
                .onErrorMap(ex -> toClientException(ex, "for book " + id));
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        return webClient.get()
                .uri("/books")
                .retrieve()
                .bodyToFlux(BookApiResponse.class)
                .timeout(Duration.ofSeconds(3))
                .map(this::toDto)
                .onErrorMap(ex -> toClientException(ex, "while fetching all books"));
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        return Mono.zip(getBookAsync(id1), getBookAsync(id2))
                .map(tuple -> List.of(tuple.getT1(), tuple.getT2()));
    }

    private BookDto toDto(BookApiResponse response) {
        return new BookDto(response.title(), response.author(), response.genre(), response.price());
    }

    private ClientException toClientException(Throwable ex, String context) {
        if (ex instanceof ClientException clientException) {
            return clientException;
        }
        if (ex instanceof WebClientResponseException responseEx) {
            if (responseEx.getStatusCode().is4xxClientError()) {
                return new ClientException("Client error " + responseEx.getStatusCode() + " " + context, ex);
            }
            if (responseEx.getStatusCode().is5xxServerError()) {
                return new ClientException("External service error " + responseEx.getStatusCode() + " " + context, ex);
            }
        }
        if (ex instanceof WebClientRequestException || ex instanceof TimeoutException) {
            return new ClientException("External service unreachable " + context, ex);
        }
        return new ClientException("Invalid response " + context, ex);
    }
}
