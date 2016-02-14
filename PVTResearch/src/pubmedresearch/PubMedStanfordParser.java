package pubmedresearch;

import java.io.StringReader;
import java.util.List;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class PubMedStanfordParser {

	private final static String PCFG = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		
	public LexicalizedParser getLexicalParser(){
		
		return LexicalizedParser.loadModel(PCFG);
	
	}
	
	public String createTree(LexicalizedParser lexicalParser, String line, String regex){

		try {
		
			line = line.toLowerCase();
			for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(line))){
			
				Tree tree = lexicalParser.apply(sentence);
				TregexPattern NPpattern = TregexPattern.compile(regex);
			    TregexMatcher matcher = NPpattern.matcher(tree);
			    if (matcher.find()) {
			       Tree match = matcher.getMatch();
			       return Sentence.listToString(match.yield());
			    }
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return "empty";
		
	}

}
