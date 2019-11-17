/******************************************************************************
 * A bare-bones immutable data type for M-by-N matrices.
 *
 * Example usage:
 *
 * int[][] d = {{1, 2, 3}, {4, 5, 6}, {9, 1, 3}};
 * Matrix D = new Matrix(d);
 * D.show();
 *
 *******************************************************************************/
package lunch.g5;


public class Matrix {
    private final int nRows;             // number of rows
    private final int nCols;             // number of columns
    private int originX = 0;
    private int originY = 0;
    private final int[][] data;      // nCols-by-nRows array

    // create nCols-by-nRows matrix of 0's
    public Matrix(int nCols, int nRows) {
        this.nCols = nCols;
        this.nRows = nRows;
        data = new int[nCols][nRows];
    }

    // create matrix based on 2d array
    public Matrix(int[][] data) {
        nCols = data.length;
        nRows = data[0].length;
        this.data = new int[nCols][nRows];
        for (int x = 0; x < nCols; x++)
            for (int y = 0; y < nRows; y++)
                this.data[x][y] = data[x][y];
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
        return (indexX >= 0 && indexX < nCols) && (indexY >= 0 && indexY < nRows);
    }

    public int[] getBounds() {
        int[] bounds = {originX - nCols, nCols - originX, originY - nRows, nRows - originY};
        return bounds;
    }

    public void increment(int i, int j) {
        int indexX = originX + i;
        int indexY = originY + j;
        data[indexX][indexY]++;
    }

    // does A = B exactly?
    public boolean eq(Matrix B) {
        Matrix A = this;
        if (B.nRows != A.nRows || B.nCols != A.nCols) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < nCols; i++)
            for (int j = 0; j < nRows; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // print matrix to standard output
    public void show() {
        for (int y = 0; y < nRows; y++) { // through y's
            for (int x = 0; x < nCols; x++)
                System.out.printf("%d ", data[x][y]);
            System.out.println();
        }
    }
}
