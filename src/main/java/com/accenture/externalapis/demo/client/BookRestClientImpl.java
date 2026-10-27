package com.accenture.externalapis.demo.client;

import com.accenture.externalapis.demo.config.ExternalServiceProperties;
import com.accenture.externalapis.demo.dto.BookApiResponse;
import com.accenture.externalapis.demo.dto.BookDto;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BookRestClientImpl implements BookRestClient {

    private RestClient restClient;

    public BookRestClientImpl(RestClient.Builder builder, ExternalServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = builder
                .baseUrl(properties.baseUrl())
                // .defaultHeader("Authorization", "Bearer " + token)
                .requestFactory(factory)
                .build();
    }

    @Override
    public BookDto getBook(Long id) {
        try {
            BookApiResponse response = restClient.get()
                    .uri("/books/{id}", id)
                    .retrieve()
                    .body(BookApiResponse.class);

            if (response == null) {
                throw new ClientException("Empty response for book: " + id);
            }
            return toDto(response);
        } catch (HttpClientErrorException ex) {
            throw new ClientException("Client error " + ex.getStatusCode() + " for book " + id, ex);
        } catch (HttpServerErrorException ex) {
            throw new ClientException("External service error " + ex.getStatusCode() + " for book " + id, ex);
        } catch (ResourceAccessException ex) {
            throw new ClientException("External service unreachable for book " + id, ex);
        } catch (RestClientException ex) {
            throw new ClientException("Invalid response for book " + id, ex);
        }
    }

    @Override
    public List<BookDto> getAllBooks() {
        try {
            BookApiResponse[] response = restClient.get()
                    .uri("/books")
                    .retrieve()
                    .body(BookApiResponse[].class);

            if (response == null) {
                return List.of();
            }
            return Arrays.stream(response)
                    .map(this::toDto)
                    .toList();
        } catch (HttpClientErrorException ex) {
            throw new ClientException("Client error " + ex.getStatusCode(), ex);
        } catch (HttpServerErrorException ex) {
            throw new ClientException("External service error " + ex.getStatusCode(), ex);
        } catch (ResourceAccessException ex) {
            throw new ClientException("External service unreachable while fetching all books", ex);
        } catch (RestClientException ex) {
            throw new ClientException("Invalid response while fetching all books", ex);
        }
    }

    private BookDto toDto(BookApiResponse response) {
        return new BookDto(response.title(), response.author(), response.genre(), response.price());
    }
}
