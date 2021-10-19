# sequence_alignment
A package of dynamic-programming sequence alignment algorithms based on the works of Needleman, Wunsch, and Gotoh.

`GotohAlign` uses affine gap penalties in scoring alignments, while `LinearAlign` uses a linear gap penalty.

Reading of Blosum matrices from NCBI website is currently not supported.

Reading of fasta sequences is currently not supported.

## Example (as found in `main.java`):
```
String x = "MYPMMYPMMYPSCPCQQQG"; // protein sequence 1
String y = "MYPTCPCGGG"; // protein sequence 2
Align gotoh = new GotohAlign(x,y);
gotoh.all_alignments();
```

## Output:
```
Global alignment:
MYPMMYPMMYPSCPCQQQG
|||        :|||:: |
MYP--------TCPCGG-G
Score: 5
Semi-global alignment:
MYPMMYPMMYPSCPCQQQG
        |||:|||::: 
--------MYPTCPCGGG-
Score: 13
Local alignment
MYPMMYPMMYPSCPCQQQG
        |||:|||
        MYPTCPCGGG        
Score: 45
````
