

public class Main {
    public static void main(String[] args){
        // blosum matrix isn't meant for DNA but whatever
        String x = "MYPMMYPMMYPSCPCQQQG"; 
        String y = "MYPTCPCGGG";
        Align gotoh = new GotohAlign(x,y);
        gotoh.all_alignments();
    }
}
