

public abstract class Align {
    
    protected String x;
    protected String y;
    protected int xlen;
    protected int ylen;
    protected int score;
    protected String x_out;
    protected String y_out;
    protected String annotation; 


    public Align(String x, String y){
        this.x = x.toUpperCase();
        this.y = y.toUpperCase();
        this.xlen = x.length();
        this.ylen = y.length();
    }

    public abstract void align_global();
    public abstract void align_semiglobal();
    public abstract void align_local();
    public abstract void describe();

    protected void all_alignments(){
        this.align_global();
        this.align_semiglobal();
        this.align_local();
    }

    protected void print_alignment(){
        System.out.println(this.x_out);
        System.out.println(this.annotation);
        System.out.println(this.y_out);
        System.out.format("Score: %d\n",this.score);
    }

    // to visualize a DP matrix
    protected void print_array(int[][] array){

        String rows = " " + this.x;
        String cols = " " + " " + this.y;

        // print column names
        for (int col = 0; col < cols.length(); ++col){
            System.out.format("%4s",cols.charAt(col));
        }
        System.out.print("\n");

        for (int row = 0; row < rows.length(); ++row){

            //print row names
            System.out.print(String.format("%4s",rows.charAt(row)));

            // print score in matrix
            for (int col = 0; col < this.y.length()+1; ++col){         
                System.out.format("%4s", array[row][col]);
            }
            System.out.print("\n");
        }
    }
}
