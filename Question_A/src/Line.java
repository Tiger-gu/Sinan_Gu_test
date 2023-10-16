public class Line {
    // Design reasons: encapsulation
    private int x_1;

    private int x_2;
    
    /** 
     * Check if two lines overlap
     * 
     * @param line - the other line
     * @return true if two lines overlap. Otherwise, false
     */
    public boolean overlap(Line other) {
        if (this.x_1 > other.getSecond()) {
            return false;
        } else if (this.x_2 < other.getFirst()) {
            return false;
        } else {
            return true;
        }
    }
    
    // the following: getter methods
    public int getFirst() {
        return this.x_1;
    }

    public int getSecond() {
        return this.x_2;
    }
    
    // public consturctor
    public Line(int x1, int x2) {
        // Check if the line is valid
        // Design reasons for adding this "check" in the constructor:
        // Firstly, if dots such as (5, 5) and lines such as (5, 1) are allowed,
        // it is going to add unnecessary complications to the overlap method and
        // potentially other methods which would have been added in the real prod environment.
        // Secondly, I specically choose an unchecked excepion to throw should 
        // the "check" fail, since it is considered a bad practise to throw
        // a checked exception in the constructor. It would add extra complications
        // (i.e.try-catch block) whenever an object of this class is created.
        if (x1 >= x2) {
            throw new IllegalArgumentException("Invalid line(x1, x2): x1 should be smaller than x2");
        }
        this.x_1 = x1;
        this.x_2 = x2;
    }
    
    public static void main(String[] args) throws Exception {
    }
}
