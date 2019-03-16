package com.alibaba.csp.sentinel.fixture;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RetryRuleTest {
    @Rule
    public RetryRule retryRule = new RetryRule();

    private int counter = 0;

    @Test
    public void smokeTest() {
    }

    @Test
    @Retry
    public void firstPassSecondFailIsSuccessful() {
        Assert.assertEquals(0, (counter++) % 2);
    }

    @Test
    @Retry
    public void firstFailSecondPassIsSuccessful() {
        Assert.assertEquals(1, (counter++) % 2);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstPassSecondFailThirdFailIsSuccessful() {
        Assert.assertEquals(0, (counter++) % 3);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstFailSecondPassThirdFailIsSuccessful() {
        Assert.assertEquals(1, (counter++) % 3);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstFailSecondFailThirdPassIsSuccessful() {
        Assert.assertEquals(2, (counter++) % 3);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstPassSecondPassThirdFailIsSuccessful() {
        int current = counter++;
        assertTrue(current == 0 || current == 1);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstFailSecondPassThirdPassIsSuccessful() {
        int current = counter++;
        assertTrue(current == 1 || current == 2);
    }

    @Test
    @Retry(maxCount = 3)
    public void firstPassSecondFailThirdPassIsSuccessful() {
        int current = counter++;
        assertTrue(current == 0 || current == 2);
    }

    @Test(expected = AssertionError.class)
    @Retry
    public void twoConsecutiveFailuresFail() {
        assertEquals(2, (counter++) % 2);
    }

    @Test(expected = AssertionError.class)
    @Retry(maxCount = 3)
    public void threeConsecutiveFailuresFail() {
        assertEquals(3, (counter++) % 3);
    }
}
