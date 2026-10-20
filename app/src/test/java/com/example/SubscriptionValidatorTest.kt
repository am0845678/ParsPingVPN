package com.example

import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionValidatorTest {

    @Test
    fun testValidAllowedUrls() {
        val url1 = "https://zerofee.ok-exor.ir/sub/token123"
        val res1 = SubscriptionValidator.validateUrl(url1)
        assertTrue(res1 is ValidationResult.Valid)
        assertEquals("https://zerofee.ok-exor.ir/sub/token123", (res1 as ValidationResult.Valid).normalizedUrl)

        val url2 = "https://panelsaz.mamadhp.ir:443/sub/uuid456?flag=1"
        val res2 = SubscriptionValidator.validateUrl(url2)
        assertTrue(res2 is ValidationResult.Valid)

        val url3 = "zerofee.ok-exor.ir/sub/my-sub"
        val res3 = SubscriptionValidator.validateUrl(url3)
        assertTrue(res3 is ValidationResult.Valid)
    }

    @Test
    fun testInvalidOrDisallowedUrlsReturnRestrictedMessage() {
        val foreignUrl = "https://google.com/sub/token123"
        val resForeign = SubscriptionValidator.validateUrl(foreignUrl)
        assertTrue(resForeign is ValidationResult.Invalid)
        assertEquals(SubscriptionValidator.RESTRICTED_APP_ERROR, (resForeign as ValidationResult.Invalid).message)

        val fakeSubdomain = "https://evil-zerofee.ok-exor.ir/sub/test"
        val resSubdomain = SubscriptionValidator.validateUrl(fakeSubdomain)
        assertTrue(resSubdomain is ValidationResult.Invalid)
        assertEquals(SubscriptionValidator.RESTRICTED_APP_ERROR, (resSubdomain as ValidationResult.Invalid).message)

        val queryParamBypass = "https://evil.com/?target=zerofee.ok-exor.ir/sub/token"
        val resBypass = SubscriptionValidator.validateUrl(queryParamBypass)
        assertTrue(resBypass is ValidationResult.Invalid)
        assertEquals(SubscriptionValidator.RESTRICTED_APP_ERROR, (resBypass as ValidationResult.Invalid).message)

        val invalidPath = "https://zerofee.ok-exor.ir/api/v1/user"
        val resPath = SubscriptionValidator.validateUrl(invalidPath)
        assertTrue(resPath is ValidationResult.Invalid)
        assertEquals(SubscriptionValidator.RESTRICTED_APP_ERROR, (resPath as ValidationResult.Invalid).message)
    }

    @Test
    fun testBlankUrl() {
        val res = SubscriptionValidator.validateUrl("   ")
        assertTrue(res is ValidationResult.Invalid)
    }
}
