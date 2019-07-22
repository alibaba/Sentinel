package com.alibaba.csp.sentinel.slots.automatic;

public class SimplexMethod {

    /*   constraint matrix   */
    private static double A[][];

    /*  the number of constraits */
    private static int m;

    /*  the number of variables */
    private static int n;

    private static double C[]; // 价值系数

    private double b[]; // 资源常数

    private double theta[]; //b的检验数

    /*  basic variables */
    private int basedVar[];


    private double yita[]; //检验数，有n个决策变量的检验数

    private double result = -1; //结果

    private int idxOfIn = -1; //换入变量的下标

    private int idxOfOut = -1; //换出变量的下标

    public SimplexMethod(double A[][],double C[],double b[]) {
        this.A = A;
        this.C = C;
        this.b = b;
        this.m = A.length;
        this.n = A[0].length;
        this.theta = new double[m];
        this.basedVar = new int[m];
        this.yita  = new double[n];
    }

    public double[] solution() {
        inputNums();
        //找初始基变量
        findBasedVariables();
        //判断是否最优解

        while (!isOptimum()) {
            //找换入变量
            idxOfIn = getVariableIn();
            //printVector();
            //找换出变量
            idxOfOut = getVariableOut();
            //如果idxOfOut返回-1，则该线性规划问题有无界解
            if(idxOfOut == -1)
                return null;
            //旋转运算，更新矩阵
            updateVectors();
            //printVector();
            //System.out.println("\n");
        }
        //输出最优解
        return printOptimum();
    }



    // 输入数据，把初始检验数赋值为价值系数
    private void inputNums() {
        for (int i = 0; i < yita.length; i++) {
            yita[i] = C[i]; //yita为检验数
        }
    }

    // 找基变量，简单的拿最后m个决策变量，后期可优化，存储在basedVar数组中
    private void findBasedVariables() {

        //取n个决策变量的最后m个作基变量
        for (int i = 0; i < m; i++) {
            //basedVar[i] = n-i;
            //改变存放顺序为正叙
            basedVar[m-i-1] = n-i ;
        }
        for (int i = 0; i < basedVar.length; i++) {
            //System.out.print("x" + (basedVar[i]) + "\t");
        }
        //System.out.println();
    }

    // 判断是否最优解，并计算检验数yita向量
    private boolean isOptimum() {
        //换入变量代替换出变量
        if(idxOfIn != -1 && idxOfOut != -1){
            //第idxOfOut个基变量换为x idxOfIn
            basedVar[idxOfOut] = idxOfIn+1;
        }
        //更新检验数
        for (int i = 0; i < n; i++) {
            double temp = C[i];
            for (int j = 0; j < m; j++) {
                temp -= A[j][i] * C[basedVar[j] -1];
            }
            yita[i] = temp;
        }

        boolean hasPossitiveYita = false;
        for (int i = 0; i < yita.length; i++) {
            if(yita[i] > 0)
                hasPossitiveYita = true;
        }
        return !hasPossitiveYita;
    }

    // 确定换入变量，返回换入变量的下标-1
    private int getVariableIn() {
        //遍历检验数
        int index = 0;
        for (int i = 0; i < yita.length; i++) {
            //System.out.print(yita[i] + "\t");
            if(yita[i] > yita[index]){
                index = i;
            }
        }

        return index;
    }

    // 确定换出变量，返回换出变量在基变量向量中的下标
    private int getVariableOut() {

        //System.out.println("theta：");
        for (int i = 0; i < m; i++) {
            if( Double.compare(A[i][idxOfIn], 0) != 0)
                theta[i] = b[i] / A[i][idxOfIn];
            else {
                theta[i] = 0;
            }
            //System.out.print(theta[i] + "\t");
        }
        //System.out.println();

        int index = 0;
        double minTheta = Double.MAX_VALUE;
        for (int i = 0; i < theta.length; i++) {
            if(theta[i] <= 0){
                continue;
            }else {
                if(theta[i] < minTheta) {
                    minTheta = theta[i];
                    index = i;
                }
            }
        }
        return index;
    }

    // 更新旋转运算后的矩阵
    private void updateVectors() {
        //m个方程，n个变量
        //将主元系数化为1
        //防止迭代中主元的值被改变后引起 其它系数除主元的新值，将主元的原值存于temp
        double temp = A[idxOfOut][idxOfIn];
        for (int i = 0; i < n; i++) {
            A[idxOfOut][i] /= temp;
        }
        b[idxOfOut] /= temp;

        //printVector();
        //主元所在列其余元素系数要化为0，即：主元列中，非主元所在行均减去 主元系数分之一*A[m][n]
        for (int i = 0; i < m; i++) {
            //若是换出变量所对应行，则该行不用换算
            double temp1 = A[i][idxOfIn]/A[idxOfOut][idxOfIn];
            if(i != idxOfOut){
                for (int j = 0; j < n; j++) {
                    A[i][j] -= A[idxOfOut][j]*temp1;
                }
                b[i] -= b[idxOfOut] * temp1;
            }
        }
    }

    //输出最优解
    private double[] printOptimum() {
        double[] x = new double[3];
        result = 0;
        for (int i = 0; i < basedVar.length; i++) {
            result += C[basedVar[i]-1] * b[i];
            //System.out.println("x"+basedVar[i]+" = "+b[i]);
            //只返回前3个x
            if(basedVar[i]==1)
                x[0]=b[i];
            if(basedVar[i]==2)
                x[1]=b[i];
            if(basedVar[i]==3)
                x[2]=b[i];
        }
        //System.out.println("最优解：z = " + result);
        return x;
    }
}

