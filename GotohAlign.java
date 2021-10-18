// alignment algorithms using affine gap penalties
// aka Gotoh algorithm
public class GotohAlign extends Align { // implements Local, Global, Semiglobal {
    
    private int[][] X;
    private int[][] Y;
    private int[][] Z;

    public GotohAlign(String x, String y) {
        super(x,y);
    }

    private void reset_state(){
        this.x_out = "";
        this.y_out = "";
        this.annotation = "";
        this.score = 0;
        this.X = new int[x.length()+1][y.length()+1];
        this.Y = new int[x.length()+1][y.length()+1]; 
        this.Z = new int[x.length()+1][y.length()+1];
    }

    // global affine base cases
    private void initialize_global(){

        reset_state();

        for (int i = 0; i < this.xlen+1; ++i){
            // prevent insertion of gap to start of x
            this.X[i][0] = -99; 
            this.Y[i][0] = -99;
            // prevent any letter in x from being discarded
            this.Z[i][0] = -99;
        }

        for (int j = 0; j < this.ylen+1; ++j){
            // prevent insertion of gap to start of y
            this.X[0][j] = -99;
            this.Y[0][j] = -99;
            // prevent any letter in y from being discarded
            this.Z[0][j] = -99;
        }

        // aligning two empty strings
        this.Z[0][0] = 0;
    }

    // semiglobal affine base cases
    private void initialize_semiglobal(){

        reset_state();
        
        for (int i = 0; i < this.xlen+1; ++i){
            // discourage insertion of gaps to start of x
            this.X[i][0] = (i==0) ? 0 : -12-2*(i-1); 
            this.Y[i][0] = (i==0) ? 0 : -12-2*(i-1);

            // prevent any letter in x from being discarded
            this.Z[i][0] = -99;
        }

        for (int j = 0; j < this.ylen+1; ++j){
            // discourage insertion of gap to start of y
            this.X[0][j] = (j==0) ? 0 : -12-2*(j-1);
            this.Y[0][j] = (j==0) ? 0 : -12-2*(j-1);

            // prevent any letter in y from being discarded
            this.Z[0][j] = -99;
        }

        // aligning two empty strings
        this.Z[0][0] = 0;
    }

    // local affine base cases
    private void initialize_local(){
        
        reset_state();
        
        for (int i = 0; i < this.xlen+1; ++i){
            // freely allow insertion of gaps to start of x
            this.X[i][0] = 0; 
            this.Y[i][0] = 0;
            // prevent any letter in x from being discarded
            this.Z[i][0] = -99;
        }

        for (int j = 0; j < this.ylen+1; ++j){
            // freely allow insertion of gap to start of y
            this.X[0][j] = 0;
            this.Y[0][j] = 0;

            // prevent any letter in y from being discarded
            this.Z[0][j] = -99;
        }

        // aligning two empty strings
        this.Z[0][0] = 0;

    }

    // tabulate scores using optimal subproblem recurrence relations
    // gap opening and gap extension penalties are currently hard coded (-12,-2)
    private void forward_pass(){

        // recurrence
        for (int i = 1; i < this.xlen+1; ++i){
            for (int j = 1; j < this.ylen+1; ++j){
                // best score if I open a gap in x
                this.X[i][j] = Math.max(this.X[i ][j-1], 
                               Math.max(this.Y[i ][j-1] - 12,
                                        this.Z[i ][j-1] - 12)) - 2;
                
                // best score if I open a gap in y
                this.Y[i][j] = Math.max(this.X[i-1][j ] - 12,
                               Math.max(this.Y[i-1][j ],
                                        this.Z[i-1][j ] - 12)) - 2;

                // best score if I match xi and yi
                int w = this.W[this.x.charAt(i-1)-'A'][this.y.charAt(j-1)-'A'];
                this.Z[i][j] = Math.max(this.X[i-1][j-1],
                               Math.max(this.Y[i-1][j-1],
                                        this.Z[i-1][j-1])) + w;
            }
        }
    }

    // i, j - starting coordinates for backtracking
    private int[] backward_pass_global(int i, int j){

        this.score = Z[i][j]; // final alignment score

        int max; // highest value amongst X, Y, and Z at (i,j)

        while (i > 0 && j > 0){

            max = Math.max(this.X[i][j],
                  Math.max(this.Y[i][j],
                           this.Z[i][j]));
            
            if (max == this.X[i][j]){
                this.x_out = "-" + this.x_out;
                this.y_out = this.y.charAt(j-1) + this.y_out;
                this.annotation = " " + this.annotation;
                --j;
            } else {
                if (max == this.Y[i][j]){
                    this.x_out = this.x.charAt(i-1) + this.x_out;
                    this.y_out = "-" + this.y_out;
                    this.annotation = " " + this.annotation;
                    --i;
                } else {
                    this.x_out = this.x.charAt(i-1) + this.x_out;
                    this.y_out = this.y.charAt(j-1) + this.y_out;
                    if (this.x.charAt(i-1)==this.y.charAt(j-1)){
                        this.annotation = "|" + this.annotation; // match
                    } else {
                        this.annotation = ":" + this.annotation; // substitution
                    }
                    --i;
                    --j;
                }
            }
        }
        return new int[] {i,j};
    }

    public void backward_pass_semiglobal(){

        // backtracking using semiglobal algorithm
        // start from either bottom row or rightmost column
        int[][] last_row = new int[1][this.ylen];
        int[][] last_col = new int[this.xlen][1];

        for (int t = 0; t < this.ylen; ++t){
            last_row[0][t] = this.Z[this.xlen][t];
        }
        for (int t = 0; t < this.xlen; ++t){
            last_col[t][0] = this.Z[t][this.ylen];
        }
        int max_row = max_idx(last_col)[0];
        int max_col = max_idx(last_row)[1];

        int i,j; // coordinates of highest score in {last row, last column}

        // step 1. handle gaps at the end of either sequence

        if (this.Z[max_row][this.ylen] < this.Z[this.xlen][max_col]){
            // gap is present at end of sequence y
            i = this.xlen;
            j = max_col;
            for (int t = this.ylen; t > max_col; --t){
                this.x_out = "-" + this.x_out;
                this.annotation = " " + this.annotation;
                this.y_out = this.y.charAt(t-1) + this.y_out;
            }
        } else {
            // gap is present at end of sequence x
            i = max_row;
            j = this.ylen;
            for (int t = this.xlen; t > max_row; --t){
                this.x_out = this.x.charAt(t-1) + this.x_out;
                this.annotation = " " + this.annotation;
                this.y_out = "-" + this.y_out;
            }
        }

        // step 2. apply global algorithm for the middle of alignment
        // update resulting i and j after global alighment
        int[] post_align_ij = this.backward_pass_global(i,j);
        i = post_align_ij[0];
        j = post_align_ij[1];

        // step 3. handle gaps at the start of either sequence (if present)
        // 3.1 either a gap was added to start of sequence x
        if (j > 0){
            for (int t = j; t > 0; --t){
                this.x_out = "-" + this.x_out;
                this.annotation = " " + this.annotation;
                this.y_out = this.y.charAt(t-1) + this.y_out;
            }
        }

        // 3.2 or gap was added to start of sequence y
        if (i > 0){
            for (int t = i; t > 0; --t){
                this.x_out = this.x.charAt(t-1) + this.x_out;
                this.annotation = " " + this.annotation;
                this.y_out = '-' + this.y_out;
            }
        }
    }

    @Override
    // solve subproblems using global forward pass alg
    // backtrack from last cell
    public void align_global(){

        System.out.println("Global alignment:");

        this.initialize_global();
        this.forward_pass(); 
        this.backward_pass_global(this.xlen, this.ylen);
        this.print_alignment();
        
    }

    @Override
    // solve subproblems using global forward pass alg
    // backtrack from some cell in the last row or last column
    public void align_semiglobal(){

        System.out.println("Semi-global alignment:");

        this.initialize_semiglobal();
        this.forward_pass();
        this.backward_pass_semiglobal();
        this.print_alignment();

    }

    // helper function find argmax of array
    private int[] max_idx(int[][] array){
        int max_i = -1;
        int max_j = -1;
        int max_val = -99;
        
        for (int i = 0; i < array.length; ++i){
            for (int j = 0; j < array[i].length; ++j){

                if (array[i][j] > max_val){
                    max_i = i;
                    max_j = j;
                    max_val = array[i][j];
                }

            }
        }
        return new int[] {max_i,max_j};
    }

    private void backward_pass_local(){

        int[] IJ = max_idx(this.Z);
        int i = IJ[0];
        int j = IJ[1];
        this.score = Z[i][j];

        // add padding to the right of alignment
        String lpad = new String(new char[Math.abs(i-j)]).replace("\0"," ");
        if (i < j){
            this.x_out = this.x.substring(i,this.xlen) + lpad;
            this.y_out = this.y.substring(j,this.xlen);
        } else {
            this.x_out = this.x.substring(i,this.xlen);
            this.y_out = this.y.substring(j,this.ylen) + lpad;
        }
        
        // local alignment segment
        int max;
        while (i > 0 && j > 0){
            max = Math.max(this.X[i][j],
                  Math.max(this.Y[i][j],
                           this.Z[i][j]));
            if (max < 0) {
                break;
            }
            if (max == this.X[i][j]){
                this.x_out = "-" + this.x_out;
                this.y_out = this.y.charAt(j-1) + this.y_out;
                this.annotation = " " + this.annotation;
                --j;
            } else {
                if (max == this.Y[i][j]){
                    this.x_out = this.x.charAt(i-1) + this.x_out;
                    this.y_out = "-" + this.y_out;
                    this.annotation = " " + this.annotation;
                    --i;
                } else {
                    this.x_out = this.x.charAt(i-1) + this.x_out;
                    this.y_out = this.y.charAt(j-1) + this.y_out;
                    if (this.x.charAt(i-1)==this.y.charAt(j-1)){
                        this.annotation = "|" + this.annotation; // match
                    } else {
                        this.annotation = ":" + this.annotation; // substitution
                    }
                    --i;
                    --j;
                }
            }
        }
        
        // add pading to the left of alignment
        String rpad = new String(new char[Math.abs(i-j)]).replace("\0"," ");
        if (i < j){
            this.x_out = rpad + this.x.substring(0,i) + this.x_out;
            this.y_out = this.y.substring(0,j) + this.y_out;
        } else {
            this.x_out = this.x.substring(0,i) + this.x_out;
            this.y_out = rpad + this.y.substring(0,j) + this.y_out;
        }
        this.annotation = rpad + this.annotation;

    }

    @Override
    public void align_local(){
        System.out.println("Local alignment");

        this.initialize_local();
        this.forward_pass();
        this.backward_pass_local();
        this.print_alignment();

    }

    @Override
    public void describe(){

        System.out.println("Best scores for alignment x--- to ***y");
        this.print_array(this.X);

        System.out.println("Best scores for alignment ***x to y---");
        this.print_array(this.Y);

        System.out.println("Best scores for alignment ***x to ***y");
        this.print_array(this.Z);

    }

}
