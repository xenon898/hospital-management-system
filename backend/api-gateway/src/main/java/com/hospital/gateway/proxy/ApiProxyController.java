package com.hospital.gateway.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Set;

/**
 * Beginner note:
 * This gateway uses simple REST proxying (RestTemplate) to keep architecture easy to explain.
 */
@RestController
public class ApiProxyController {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "content-length", "host", "keep-alive", "proxy-authenticate",
            "proxy-authorization", "proxy-connection", "te", "trailer",
            "transfer-encoding", "upgrade"
    );

    private final RestTemplate restTemplate;

    public ApiProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${services.user.url}")
    private String userUrl;

    @Value("${services.doctor.url}")
    private String doctorUrl;

    @Value("${services.patient.url}")
    private String patientUrl;

    @Value("${services.appointment.url}")
    private String appointmentUrl;

    @RequestMapping(value = "/api/users/**")
    public ResponseEntity<byte[]> proxyUsers(HttpServletRequest request) {
        return proxyTo(userUrl, request);
    }

    @RequestMapping(value = "/api/doctors/**")
    public ResponseEntity<byte[]> proxyDoctors(HttpServletRequest request) {
        return proxyTo(doctorUrl, request);
    }

    @RequestMapping(value = "/api/patients/**")
    public ResponseEntity<byte[]> proxyPatients(HttpServletRequest request) {
        return proxyTo(patientUrl, request);
    }

    @RequestMapping(value = "/api/appointments/**")
    public ResponseEntity<byte[]> proxyAppointments(HttpServletRequest request) {
        return proxyTo(appointmentUrl, request);
    }

    private ResponseEntity<byte[]> proxyTo(String baseUrl, HttpServletRequest request) {
        try {
            String target = baseUrl + request.getRequestURI();
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }

            HttpHeaders headers = new HttpHeaders();
            var auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null) headers.set(HttpHeaders.AUTHORIZATION, auth);

            for (String headerName : java.util.Collections.list(request.getHeaderNames())) {
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName) || isHopByHop(headerName)) continue;
                headers.add(headerName, request.getHeader(headerName));
            }

            byte[] body = request.getInputStream().readAllBytes();
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(target),
                    method,
                    new org.springframework.http.HttpEntity<>(body, headers),
                    byte[].class
            );
            return forwardedResponse(response.getStatusCode(), response.getHeaders(), response.getBody());
        } catch (HttpStatusCodeException e) {
            return forwardedResponse(e.getStatusCode(), e.getResponseHeaders(), e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(500).body((e.getMessage() == null ? "" : e.getMessage()).getBytes());
        }
    }

    private ResponseEntity<byte[]> forwardedResponse(org.springframework.http.HttpStatusCode status,
                                                     HttpHeaders downstreamHeaders, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        downstreamHeaders.forEach((headerName, values) -> {
            if (!isHopByHop(headerName)) {
                headers.put(headerName, values);
            }
        });
        return ResponseEntity.status(status).headers(headers).body(body);
    }

    private boolean isHopByHop(String headerName) {
        return HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase(java.util.Locale.ROOT));
    }
}

