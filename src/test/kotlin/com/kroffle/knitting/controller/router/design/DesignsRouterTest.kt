package com.kroffle.knitting.controller.router.design

import com.fasterxml.jackson.databind.ObjectMapper
import com.kroffle.knitting.controller.filter.auth.AuthorizationFilter
import com.kroffle.knitting.controller.handler.design.DesignHandler
import com.kroffle.knitting.domain.design.entity.Design
import com.kroffle.knitting.domain.design.enum.DesignType
import com.kroffle.knitting.domain.design.enum.PatternType
import com.kroffle.knitting.infra.design.entity.DesignEntity
import com.kroffle.knitting.usecase.design.DesignRepository
import com.kroffle.knitting.usecase.design.DesignService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.util.UUID

@WebFluxTest
@ExtendWith(SpringExtension::class)
class DesignsRouterTest {

    private lateinit var webClient: WebTestClient

    private lateinit var design: Design

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var repo: DesignRepository

    @MockBean
    lateinit var tokenDecoder: AuthorizationFilter.TokenDecoder

    @BeforeEach
    fun setUp() {
        design = DesignEntity(
            id = UUID.randomUUID(),
            name = "test",
            designType = DesignType.Sweater,
            patternType = PatternType.Text,
            stitches = 23.5,
            rows = 25.0,
            totalLength = 1.0,
            sleeveLength = 2.0,
            shoulderWidth = 3.0,
            bottomWidth = 4.0,
            armholeDepth = 5.0,
            needle = "5.0mm",
            yarn = null,
            extra = null,
            price = 0,
            pattern = "# Step1. 코를 10개 잡습니다.",
            createdAt = LocalDateTime.now(),
        ).toDesign()

        val routerFunction = DesignsRouter(DesignHandler(DesignService(repo))).designsRouterFunction()
        webClient = WebTestClient.bindToRouterFunction(routerFunction).build()
    }

    @Test
    fun `design 리스트가 잘 반환되어야 함`() {
        given(repo.getAll()).willReturn(Flux.just(design))
        val responseBody: List<Design>? = webClient
            .get()
            .uri("/designs/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(Design::class.java)
            .hasSize(1)
            .returnResult()
            .responseBody

        val firstResponseBody = responseBody!![0]
        assertThat(firstResponseBody.id).isEqualTo(design.id)
        assertThat(firstResponseBody.name).isEqualTo("test")
        assertThat(firstResponseBody.designType).isEqualTo(DesignType.Sweater)
        assertThat(firstResponseBody.patternType).isEqualTo(PatternType.Text)
        assertThat(firstResponseBody.gauge.stitches).isEqualTo(23.5)
        assertThat(firstResponseBody.gauge.rows).isEqualTo(25.0)
        assertThat(firstResponseBody.needle).isEqualTo("5.0mm")
        assertThat(firstResponseBody.yarn).isEqualTo(null)
        assertThat(firstResponseBody.extra).isEqualTo(null)
        assertThat(firstResponseBody.price.value).isEqualTo(0)
        assertThat(firstResponseBody.size.totalLength.value).isEqualTo(1.0)
        assertThat(firstResponseBody.size.sleeveLength.value).isEqualTo(2.0)
        assertThat(firstResponseBody.size.shoulderWidth.value).isEqualTo(3.0)
        assertThat(firstResponseBody.size.bottomWidth.value).isEqualTo(4.0)
        assertThat(firstResponseBody.size.armholeDepth.value).isEqualTo(5.0)
        assertThat(firstResponseBody.pattern.value).isEqualTo("# Step1. 코를 10개 잡습니다.")
        assertThat(firstResponseBody.createdAt).isEqualTo(design.createdAt)
    }
}
