package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// TODO: Make this class implement BookWebClient.
@Component
public class BookWebClientImpl implements BookWebClient {

    private WebClient webClient;

    public BookWebClientImpl(WebClient.Builder builder, ExternalServiceProperties properties) {
        this.webClient = builder
                .baseUrl(properties.baseUrl())
                // .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Override
    public Mono<BookDto> getBookAsync(Long id) {
        return Mono.error(new UnsupportedOperationException("not implemented"));
    }

    @Override
    public Flux<BookDto> getAllBooksAsync() {
        return Flux.error(new UnsupportedOperationException("not implemented"));
    }

    @Override
    public Mono<List<BookDto>> getBooksInParallel(Long id1, Long id2) {
        return Mono.error(new UnsupportedOperationException("not implemented"));
    }

    // TODO: Implement getBookAsync(Long id) - fetch one book from GET /books/{id} as
    // Mono<BookApiResponse>, then map it onto a Mono<BookDto>.
    //
    // TODO: Handle the main WebClient error cases and rethrow them as ClientException,
    // e.g. via onStatus()/onErrorResume():
    //  - WebClientResponseException (4xx/5xx, e.g. book not found or the faulty/teapot book)
    //  - WebClientRequestException (connection refused / timeout - the external service is unreachable)

    // TODO: Implement getAllBooksAsync() - fetch all books from GET /books as
    // Flux<BookApiResponse>, then map each one onto a BookDto. Handle the same error
    // cases as getBookAsync() above.

    // TODO: Implement getBooksInParallel(Long id1, Long id2) - fetch two books in
    // parallel with Mono.zip(). Handle the same error cases as getBookAsync() above.
}
