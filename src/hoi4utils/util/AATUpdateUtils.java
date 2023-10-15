package hoi4utils.util;

import java.io.File;

import hoi4utils.HOIIVFile;
import hoi4utils.clausewitz_parser.Expression;
import hoi4utils.clausewitz_parser.Parser;

public class AATUpdateUtils {
    double MULTIPLIER = 3.00 ;
	public void fixconsumergoodsfactor () {
        File ideadirectory = HOIIVFile.ideas_folder ;
        for ( File ideaFile: ideadirectory.listFiles () ) {
            Parser parser = new Parser ( ideaFile );
            Expression[] exp = parser.expression().getAllSubexpressions ( "consumer_goods_factor=" );
            for ( Expression simon:exp ) {
               double v = simon.getDoubleValue() ;
                v *= MULTIPLIER;
                
            }
        }
    }
}
