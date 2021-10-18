

public class Main {
    public static void main(String[] args){
        String x = "MYPMMYPMMYPSCPCQQQG"; // protein sequence 1
        String y = "MYPTCPCGGG"; // protein sequence 2
        Align gotoh = new GotohAlign(x,y);
        gotoh.all_alignments();
    }
}
