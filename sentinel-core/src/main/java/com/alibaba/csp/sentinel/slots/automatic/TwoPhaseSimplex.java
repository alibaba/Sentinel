package com.alibaba.csp.sentinel.slots.automatic;


/**
 *  Given an m-by-n matrix A, an m-length vector b, and an
 *  n-length vector c, solve the  LP { max cx : Ax <= b, x >= 0 }.
 *  Unlike Simplex.java, this version does not assume b >= 0,
 *  so it needs to find a basic feasible solution in Phase I.
 *
 *  Creates an (m+1)-by-(n+m+1) simplex tableaux with the
 *  RHS in column m+n, the objective function in row m, and
 *  slack variables in columns m through m+n-1.
 *
 *  From: https://algs4.cs.princeton.edu/65reductions/
 *
 *  @author Li Yudong
 */
public class TwoPhaseSimplex {
    private static final double EPSILON = 1.0E-8;

    /**
     * tableaux
     */
    private double[][] a;

    /**
     * number of constraints
     */
    private int m;

    /**
     * number of original variables
     */
    private int n;

    /**
     * basis[i] = basic variable corresponding to row i
     */
    private int[] basis;

    /**
     * sets up the simplex tableaux
     */
    public TwoPhaseSimplex(double[][] A, double[] b, double[] c) {
        m = b.length;
        n = c.length;
        a = new double[m+2][n+m+m+1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = A[i][j];
            }
        }
        for (int i = 0; i < m; i++) {
            a[i][n + i] = 1.0;
        }
        for (int i = 0; i < m; i++){
            a[i][n+m+m] = b[i];
        }
        for (int j = 0; j < n; j++) {
            a[m][j] = c[j];
        }

        // if negative RHS, multiply by -1
        for (int i = 0; i < m; i++) {
            if (b[i] < 0) {
                for (int j = 0; j <= n+m+m; j++) {
                    a[i][j] = -a[i][j];
                }
            }
        }

        // artificial variables form initial basis
        for (int i = 0; i < m; i++) {
            a[i][n + m + i] = 1.0;
        }
        for (int i = 0; i < m; i++) {
            a[m + 1][n + m + i] = -1.0;
        }
        for (int i = 0; i < m; i++) {
            pivot(i, n + m + i);
        }

        basis = new int[m];
        for (int i = 0; i < m; i++) {
            basis[i] = n + m + i;
        }

        phase1();

        phase2();

        // check optimality conditions
        assert check(A, b, c);
    }

    /**
     * run phase I simplex algorithm to find basic feasible solution
     */
    private void phase1() {
        while (true) {

            // find entering column q
            int q = bland1();
            // optimal
            if (q == -1) {break;}

            // find leaving row p
            int p = minRatioRule(q);
            assert p != -1 : "Entering column = " + q;

            // pivot
            pivot(p, q);

            // update basis
            basis[p] = q;
            // show();
        }
        if (a[m+1][n+m+m] > EPSILON) {throw new ArithmeticException("Linear program is infeasible");}
    }


    /**
     *     run simplex algorithm starting from initial basic feasible solution
     */
    private void phase2() {
        while (true) {

            // find entering column q
            int q = bland2();
            if (q == -1){
                break;  // optimal
            }

            // find leaving row p
            int p = minRatioRule(q);
            if (p == -1) {
                throw new ArithmeticException("Linear program is unbounded");
            }

            // pivot
            pivot(p, q);

            // update basis
            basis[p] = q;
        }
    }

    /**
     * lowest index of a non-basic column with a positive cost -
     * using artificial objective function
     */
    private int bland1() {
        for (int j = 0; j < n+m; j++) {
            if (a[m + 1][j] > EPSILON) {
                return j;
            }
        }
        // optimal
        return -1;
    }

    /**
     * lowest index of a non-basic column with a positive cost
     */
    private int bland2() {
        for (int j = 0; j < n+m; j++) {
            if (a[m][j] > EPSILON){
                return j;
            }
        }
        // optimal
        return -1;
    }


    /**
     * find row p using min ratio rule (-1 if no such row)
     */
    private int minRatioRule(int q) {
        int p = -1;
        for (int i = 0; i < m; i++) {
            // if (a[i][q] <= 0) continue;
            if (a[i][q] <= EPSILON) {continue;}
            else if (p == -1) {p = i;}
            else if ((a[i][n+m+m] / a[i][q]) < (a[p][n+m+m] / a[p][q])) {p = i;}
        }
        return p;
    }

    /**
     * pivot on entry (p, q) using Gauss-Jordan elimination
     */
    private void pivot(int p, int q) {

        // everything but row p and column q
        for (int i = 0; i <= m+1; i++) {
            for (int j = 0; j <= n + m + m; j++) {
                if (i != p && j != q) {a[i][j] -= a[p][j] * a[i][q] / a[p][q];}
            }
        }

        // zero out column q
        for (int i = 0; i <= m+1; i++) {
            if (i != p) {a[i][q] = 0.0;}
        }

        // scale row p
        for (int j = 0; j <= n+m+m; j++) {
            if (j != q) {a[p][j] /= a[p][q];}
        }
        a[p][q] = 1.0;
    }

    /**
     *     return optimal objective value
     */
    public double value() {
        return -a[m][n+m+m];
    }

    /**
     *     return primal solution vector
     */
    public double[] primal() {
        double[] x = new double[n];
        for (int i = 0; i < m; i++) {
            if (basis[i] < n) {x[basis[i]] = a[i][n + m + m];}
        }
        return x;
    }

    /**
     * return dual solution vector
     */
    public double[] dual() {
        double[] y = new double[m];
        for (int i = 0; i < m; i++) {
            y[i] = -a[m][n + i];
        }
        return y;
    }


    /**
     * is the solution primal feasible?
     */
    private boolean isPrimalFeasible(double[][] A, double[] b) {
        double[] x = primal();

        // check that x >= 0
        for (int j = 0; j < x.length; j++) {
            if (x[j] < 0.0) {
                System.out.println("x[" + j + "] = " + x[j] + " is negative");
                return false;
            }
        }

        // check that Ax <= b
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            if (sum > b[i] + EPSILON) {
                System.out.println("not primal feasible");
                System.out.println("b[" + i + "] = " + b[i] + ", sum = " + sum);
                return false;
            }
        }
        return true;
    }

    /**
     * is the solution dual feasible?
     */
    private boolean isDualFeasible(double[][] A, double[] c) {
        double[] y = dual();

        // check that y >= 0
        for (int i = 0; i < y.length; i++) {
            if (y[i] < 0.0) {
                System.out.println("y[" + i + "] = " + y[i] + " is negative");
                return false;
            }
        }

        // check that yA >= c
        for (int j = 0; j < n; j++) {
            double sum = 0.0;
            for (int i = 0; i < m; i++) {
                sum += A[i][j] * y[i];
            }
            if (sum < c[j] - EPSILON) {
                System.out.println("not dual feasible");
                System.out.println("c[" + j + "] = " + c[j] + ", sum = " + sum);
                return false;
            }
        }
        return true;
    }

    /**
     * check that optimal value = cx = yb
     */
    private boolean isOptimal(double[] b, double[] c) {
        double[] x = primal();
        double[] y = dual();
        double value = value();

        // check that value = cx = yb
        double value1 = 0.0;
        for (int j = 0; j < x.length; j++) {
            value1 += c[j] * x[j];
        }
        double value2 = 0.0;
        for (int i = 0; i < y.length; i++) {
            value2 += y[i] * b[i];
        }
        if (Math.abs(value - value1) > EPSILON || Math.abs(value - value2) > EPSILON) {
            System.out.println("value = " + value + ", cx = " + value1 + ", yb = " + value2);
            return false;
        }

        return true;
    }

    private boolean check(double[][]A, double[] b, double[] c) {
        return isPrimalFeasible(A, b) && isDualFeasible(A, c) && isOptimal(b, c);
    }

    /**
     * print tableaux
     */
    public void show() {
        System.out.println("m = " + m);
        System.out.println("n = " + n);
        for (int i = 0; i <= m+1; i++) {
            for (int j = 0; j <= n+m+m; j++) {
                System.out.printf("%7.2f ", a[i][j]);
                if (j == n+m-1 || j == n+m+m-1) {System.out.print(" |");}
            }
            System.out.println();
        }
        System.out.print("basis = ");
        for (int i = 0; i < m; i++) {
            System.out.print(basis[i] + " ");
        }
        System.out.println();
        System.out.println();
    }


    public static void test(double[][] A, double[] b, double[] c) {
        TwoPhaseSimplex lp;
        try {
            lp = new TwoPhaseSimplex(A, b, c);
        }
        catch (ArithmeticException e) {
            System.out.println(e);
            return;
        }
        System.out.println("value = " + lp.value());
        double[] x = lp.primal();
        for (int i = 0; i < x.length; i++) {
            System.out.println("x[" + i + "] = " + x[i]);
        }
        double[] y = lp.dual();
        for (int j = 0; j < y.length; j++) {
            System.out.println("y[" + j + "] = " + y[j]);
        }
    }

}
