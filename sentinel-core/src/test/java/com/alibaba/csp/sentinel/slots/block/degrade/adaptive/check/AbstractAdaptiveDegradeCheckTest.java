package com.alibaba.csp.sentinel.slots.block.degrade.adaptive.check;

import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.circuitbreaker.AdaptiveCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.scenario.Scenario;
import com.alibaba.csp.sentinel.slots.statistic.base.WindowWrap;
import org.junit.*;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

/**
 * Unit test for {@link AbstractAdaptiveDegradeCheck} and {@link DefaultAdaptiveDegradeCheck}.
 *
 * @author ylnxwlp
 */
public class AbstractAdaptiveDegradeCheckTest {

    @Test
    public void testScenarioDispatch_CallsOverloadMethod() {
        TestableAdaptiveCheck checker = new TestableAdaptiveCheck(0.42);

        double ret = checker.getPassProbability(
                "resA",
                Scenario.SystemScenario.OVER_LOAD,
                null,
                null
        );

        Assert.assertTrue("Protected method should be invoked for OVER_LOAD", checker.called);
        Assert.assertEquals(0.42, ret, 1e-9);
    }

    @Test(expected = NullPointerException.class)
    public void testNullScenario_ThrowsNPE() {
        TestableAdaptiveCheck checker = new TestableAdaptiveCheck(0.5);
        checker.getPassProbability("res", null, null, null); // switch(null) -> NPE
    }

    @Test
    public void testNonOverloadScenario_ReturnsMinusOne_IfPresent() {
        Scenario.SystemScenario other = findAnyNonOverload();
        Assume.assumeTrue("Only OVER_LOAD exists; skipping this test.", other != null);

        TestableAdaptiveCheck checker = new TestableAdaptiveCheck(0.99);
        double ret = checker.getPassProbability("resX", other, null, null);

        Assert.assertFalse("Protected method should NOT be invoked for non-OVER_LOAD", checker.called);
        Assert.assertEquals("Should return -1 for unsupported scenarios", -1.0, ret, 1e-9);
    }


    @Test
    public void testDefaultImpl_DelegatesToProtectedMethod_AndPassesArguments() {
        @SuppressWarnings("unchecked")
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> winMock =
                (WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>) Mockito.mock(WindowWrap.class);
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> wins = Collections.singletonList(winMock);

        TestableDefaultDelegate checker = new TestableDefaultDelegate(0.77);
        String resourceName = "resource-1";

        double ret = checker.getPassProbability(resourceName, Scenario.SystemScenario.OVER_LOAD, winMock, wins);

        Assert.assertEquals(0.77, ret, 1e-9);
        Assert.assertTrue("Protected method should be invoked", checker.called);
        Assert.assertEquals(resourceName, checker.capturedResource);
        Assert.assertSame(winMock, checker.capturedCurrentWindow);
        Assert.assertSame(wins, checker.capturedWindows);
    }

    @Test
    public void testDefaultImpl_NonOverloadScenario_ReturnsMinusOne_IfPresent() {
        Scenario.SystemScenario other = findAnyNonOverload();
        Assume.assumeTrue("Only OVER_LOAD exists; skipping this test.", other != null);

        TestableDefaultDelegate checker = new TestableDefaultDelegate(0.33);
        double ret = checker.getPassProbability("r", other, null, null);

        Assert.assertEquals(-1.0, ret, 1e-9);
        Assert.assertFalse("Protected method should NOT be invoked for non-OVER_LOAD", checker.called);
    }

    private static Scenario.SystemScenario findAnyNonOverload() {
        for (Scenario.SystemScenario s : Scenario.SystemScenario.values()) {
            if (s != Scenario.SystemScenario.OVER_LOAD) {
                return s;
            }
        }
        return null;
    }

    private static class TestableAdaptiveCheck extends AbstractAdaptiveDegradeCheck {
        boolean called = false;
        final double ret;

        TestableAdaptiveCheck(double ret) { this.ret = ret; }

        @Override
        protected double getPassProbabilityWhenOverloading(
                String resourceName,
                WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
            called = true;
            return ret;
        }
    }

    private static class TestableDefaultDelegate extends DefaultAdaptiveDegradeCheck {
        boolean called = false;
        final double ret;

        String capturedResource;
        WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> capturedCurrentWindow;
        List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> capturedWindows;

        TestableDefaultDelegate(double ret) { this.ret = ret; }

        @Override
        protected double getPassProbabilityWhenOverloading(
                String resourceName,
                WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter> currentWindow,
                List<WindowWrap<AdaptiveCircuitBreaker.AdaptiveCounter>> windows) {
            called = true;
            this.capturedResource = resourceName;
            this.capturedCurrentWindow = currentWindow;
            this.capturedWindows = windows;
            return ret;
        }
    }
}
