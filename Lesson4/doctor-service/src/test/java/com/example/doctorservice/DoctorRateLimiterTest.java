package com.example.doctorservice;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DoctorRateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        // Reset or obtain rate limiter state before each test
        rateLimiterRegistry.rateLimiter("searchDoctorLimit");
    }

    @Test
    void testRateLimiterReturns429On6thRequest() throws Exception {
        // 5 lần gọi đầu tiên thành công (HTTP 200 OK)
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/api/v1/doctors"))
                    .andExpect(status().isOk());
        }

        // Lần gọi thứ 6 bị giới hạn tốc độ và trả về lỗi 429 Too Many Requests
        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isTooManyRequests());
    }
}
