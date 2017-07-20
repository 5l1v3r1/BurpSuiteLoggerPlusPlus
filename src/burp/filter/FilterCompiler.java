package burp.filter;

import burp.LogEntry;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by corey on 19/07/17.
 */
public class FilterCompiler {
    private static Pattern regexPattern = Pattern.compile("\\/(.*)\\/");
    private static Pattern bracketsPattern = Pattern.compile("(.*?)(!?)(\\(.*\\))(.*?)");
    private static Pattern compoundPattern = Pattern.compile("(.*?)(\\|+|&+)(.*?)");

    //TODO implement type parser?
    public static Object parseItem(String item) throws Filter.FilterException {
        try {
            return LogEntry.columnNamesType.valueOf(item.toUpperCase());
        }catch (IllegalArgumentException e){}

        Matcher regexMatcher = regexPattern.matcher(item);
        if(regexMatcher.matches()){
            try {
                Pattern regexItem = Pattern.compile(regexMatcher.group(1));
                return regexItem;
            }catch (PatternSyntaxException pSException){
                throw new Filter.FilterException("Invalid Regex Pattern");
            }
        }

        if(regexPattern.matcher(item).matches()){
            return item.substring(1, item.length()-1);
        }
        return item.trim();
    }

    public static Filter parseString(String string) throws Filter.FilterException {
        String regexStripped = stripRegex(string);
        Matcher bracketMatcher = bracketsPattern.matcher(regexStripped);

        if (bracketMatcher.matches()) {
            Filter group;
            boolean inverted = "!".equals(bracketMatcher.group(2));
            int startBracket = regexStripped.indexOf("(");
            int endBracket = getBracketMatch(regexStripped, startBracket);
            group = parseString(string.substring(startBracket+1, endBracket));
            group.inverted = inverted;
            Pattern leftCompound = Pattern.compile("(.*?)(\\|++|&++)\\s*$");
            Pattern rightCompound = Pattern.compile("^(\\s*)(\\|++|&++)(.*)");
            String left = string.substring(0, startBracket);
            String right = string.substring(endBracket+1, regexStripped.length());
            Matcher leftMatcher = leftCompound.matcher(left);
            Matcher rightMatcher = rightCompound.matcher(right);
            if (leftMatcher.matches()) {
                group = new CompoundFilter(parseString(leftMatcher.group(1)), leftMatcher.group(2), group);
            }
            if (rightMatcher.matches()) {
                group = new CompoundFilter(group, rightMatcher.group(2), parseString(rightMatcher.group(3)));
            }
            return group;
        } else {
            Matcher compoundMatcher = compoundPattern.matcher(string);
            if (compoundMatcher.matches()) {
                return new CompoundFilter(compoundMatcher.group(1), compoundMatcher.group(2), compoundMatcher.group(3));
            } else {
                Pattern operation = Pattern.compile("(.*?)((?:=?(?:=|<|>|!)=?))(.*?)");
                Matcher operationMatcher = operation.matcher(string);
                if(operationMatcher.matches()){
                    return new Filter(operationMatcher.group(1).trim(), operationMatcher.group(2), operationMatcher.group(3).trim());
                }
            }
        }
        throw new Filter.FilterException("Could not parse filter");
    }

    private static int getBracketMatch(String string, int start) {
        int end = start;
        int count = 1;
        while (count > 0){
            char c = string.charAt(++end);
            if (c == '('){ count++; }
            else if(c == ')'){ count--; }
        }
        return end;
    }

    private static boolean isRegex(String string){
        try{
            Pattern.compile(string);
            return true;
        }catch (PatternSyntaxException pSException){
            return false;
        }
    }

    private static String stripRegex(String string){
        Pattern hasRegex = Pattern.compile("(.*)(\\/.*\\/)(.*)");
        string = string.replace("\\\\", "  ").replace("\\/", "  ");
        Matcher matcher;
        while((matcher = hasRegex.matcher(string)).matches()) {
            string = matcher.group(1) + StringUtils.repeat(" ", matcher.group(2).length()) + matcher.group(3);
        }
        return string;
    }
}
