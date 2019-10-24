package com.alibaba.csp.sentinel.slots.automatic;

import org.junit.Test;

/**
 * @author Li Yudong
 */
public class SimplexTest {

    @Test
    public void test1() {
        double[] c = {  13.0,  23.0 };
        double[] b = { 480.0, 160.0, 1190.0 };
        double[][] A = {
                {  5.0, 15.0 },
                {  4.0,  4.0 },
                { 35.0, 20.0 },
        };
        TwoPhaseSimplex.test(A, b, c);
    }

    @Test
    public void test2() {
        double[] b = {  -13.0,  -23.0 };
        double[] c = { -480.0, -160.0, -1190.0 };
        double[][] A = {
                {  -5.0, -4.0, -35.0 },
                { -15.0, -4.0, -20.0 }
        };
        TwoPhaseSimplex.test(A, b, c);
    }

    @Test
    public void test3() {
        double[][] A = {
                { -1,  1,  0 },
                {  1,  4,  0 },
                {  2,  1,  0 },
                {  3, -4,  0 },
                {  0,  0,  1 },
        };
        double[] c = { 1, 1, 1 };
        double[] b = { 5, 45, 27, 24, 4 };
        TwoPhaseSimplex.test(A, b, c);
    }

    @Test
    public void test4() {
        double[] c = { 2.0, 3.0, -1.0, -12.0 };
        double[] b = {  3.0,   2.0 };
        double[][] A = {
                { -2.0, -9.0,  1.0,  9.0 },
                {  1.0,  1.0, -1.0, -2.0 },
        };
        TwoPhaseSimplex.test(A, b, c);
    }

    @Test
    public void test5() {
        double[] c = { 10.0, -57.0, -9.0, -24.0 };
        double[] b = {  0.0,   0.0,  1.0 };
        double[][] A = {
                { 0.5, -5.5, -2.5, 9.0 },
                { 0.5, -1.5, -0.5, 1.0 },
                { 1.0,  0.0,  0.0, 0.0 },
        };
        TwoPhaseSimplex.test(A, b, c);
    }

    @Test
    public void test6() {
        double[] c = { -1, -1, -1, -1, -1, -1, -1, -1, -1 };
        double[] b = { -0.9, 0.2, -0.2, -1.1, -0.7, -0.5, -0.1, -0.1, -1 };
        double[][] A = {
                { -2,  1,  0,  0,  0,  0,  0,  0,  0 },
                {  1, -2, -1,  0,  0,  0,  0,  0,  0 },
                {  0, -1, -2, -1,  0,  0,  0,  0,  0 },
                {  0,  0, -1, -2, -1, -1,  0,  0,  0 },
                {  0,  0,  0, -1, -2, -1,  0,  0,  0 },
                {  0,  0,  0, -1, -1, -2, -1,  0,  0 },
                {  0,  0,  0,  0,  0, -1, -2, -1,  0 },
                {  0,  0,  0,  0,  0,  0, -1, -2, -1 },
                {  0,  0,  0,  0,  0,  0,  0, -1, -2 }
        };
        TwoPhaseSimplex.test(A, b, c);
    }
}
