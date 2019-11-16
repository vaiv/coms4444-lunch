/******************************************************************************
 * A bare-bones immutable data type for M-by-N matrices.
 *
 * Example usage:
 *
 * int[][] d = {{1, 2, 3}, {4, 5, 6}, {9, 1, 3}};
 * Matrix D = new Matrix(d);
 * D.show();
 *
 * Matrix B = A.transpose();
 * B.show();
 *
 * Matrix C = Matrix.identity(5);
 * C.show();
 *
 * A.plus(B);
 * B.times(A);
 *
 * // shouldn't be equal since AB != BA in general
 * StdOut.println(A.times(B).eq(B.times(A)));
 ******************************************************************************/
package lunch.g5;


public class Matrix {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private int originX = 0;
    private int originY = 0;
    private final int[][] data;      // M-by-N array

    // create M-by-N matrix of 0's
    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new int[M][N];
    }

    // create matrix based on 2d array
    public Matrix(int[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new int[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                this.data[i][j] = data[i][j];
    }

    // copy constructor
    private Matrix(Matrix A) {
        this(A.data);
    }

    // set origin
    public void setOrigin(int x, int y) {
        originX = x;
        originY = y;
    }

    // get method for an element by index [i, j]
    public int get(int i, int j) {
        int indexX = originX + i;
        int indexY = originY + j;
        return data[indexX][indexY];
    }

    // set an element by index [i, j]
    public void set(int i, int j, int value) {
        int indexX = originX + i;
        int indexY = originY + j;
        data[indexX][indexY] = value;
    }

    public boolean has(int i, int j) {
        int indexX = originX + i;
        int indexY = originY + j;
        return (indexX >= 0 && indexX < N) && (indexY >= 0 && indexY < M);
    }

    public int[] getBounds() {
        int[] bounds = {originX - M, M - originX, originY - N, N - originY};
        return bounds;
    }

    public void increment(int i, int j) {
        int indexX = originX + i;
        int indexY = originY + j;
        data[indexX][indexY]++;
    }

    // swap rows i and j
    private void swap(int i, int j) {
        int[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
        Matrix A = new Matrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public Matrix plus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }


    // return C = A - B
    public Matrix minus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean eq(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // return C = A * B
    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }

    // print matrix to standard output
    public void show() {
        for (int y = 0; y < N; y++) { // through y's
            for (int x = 0; x < M; x++)
                System.out.printf("%d ", data[x][y]);
            System.out.println();
        }
    }
}
