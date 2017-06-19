package burp.filter;
import burp.LogEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    enum LogicalOperation {LT,LE,GT,GE,EQ,NE}
    public boolean inverted;
    public Object left;
    public LogicalOperation operation;
    public Object right;

    protected Filter(){}
    protected Filter(Object left, String operation, Object right) throws FilterException {
        LogicalOperation op;
        switch (operation){
            case "=": op = LogicalOperation.EQ;
                break;
            case "!=": op = LogicalOperation.NE;
                break;
            case "<": op = LogicalOperation.LT;
                break;
            case ">": op = LogicalOperation.GT;
                break;
            case "<=": op = LogicalOperation.LE;
                break;
            case ">=": op = LogicalOperation.GE;
                break;
            default:
                throw new FilterException("Invalid operator " + operation);
        }

        if(left instanceof String){
            if(((String) left).contains("(") || ((String) left).contains(")")) {
                throw new FilterException("Unmatched Bracket");
            }
            this.left = parseItem((String) left);
        }else{
            this.left = left;
        }
        if(right instanceof String){
            if(((String) right).contains("(") || ((String) right).contains(")")){
                throw new FilterException("Unmatched Bracket");
            }
            this.right = parseItem((String) right);
        }else{
            this.right = right;
        }

        this.operation = op;
    }

    //TODO implement type parser?
    private Object parseItem(String item){
        try {
            return LogEntry.columnNamesType.valueOf(item.toUpperCase());
        }catch (IllegalArgumentException e){}

        if(Pattern.compile("\\\"(.*)\\\"").matcher(item).matches()){
            return item.substring(1, item.length()-1);
        }
        return item;
    }

    public static Filter parseString(String string) throws FilterException{
        Pattern brackets = Pattern.compile("(.*?)(!?)(\\(.*\\))(.*?)");
        Pattern compound = Pattern.compile("(.*?)(\\|+|&+)(.*?)");
        Matcher bracketMatcher = brackets.matcher(string);

        if (bracketMatcher.matches()) {
            Filter group;
            boolean inverted = "!".equals(bracketMatcher.group(2));
            group = parseString(bracketMatcher.group(3)
                    .substring(1, bracketMatcher.group(3).length() - 1));
            group.inverted = inverted;
            Pattern leftCompound = Pattern.compile("(.*?)(\\|++|&++)\\s*$");
            Pattern rightCompound = Pattern.compile("^(\\s*)(\\|++|&++)(.*)");
            String left = bracketMatcher.group(1);
            String right = bracketMatcher.group(4);
            Matcher leftMatcher = leftCompound.matcher(left);
            Matcher rightMatcher = rightCompound.matcher(right);
            if (leftMatcher.matches()) {
                group = new CompoundFilter(Filter.parseString(leftMatcher.group(1)), leftMatcher.group(2), group);
            }
            if (rightMatcher.matches()) {
                group = new CompoundFilter(group, rightMatcher.group(2), Filter.parseString(rightMatcher.group(3)));
            }
            return group;
        } else {
            Matcher compoundMatcher = compound.matcher(string);
            if (compoundMatcher.matches()) {
                return new CompoundFilter(compoundMatcher.group(1), compoundMatcher.group(2), compoundMatcher.group(3));
            } else {
                Pattern operation = Pattern.compile("(.*?)((?:=|<|>|!)=?)(.*?)");
                Matcher operationMatcher = operation.matcher(string);
                if(operationMatcher.matches()){
                    return new Filter(operationMatcher.group(1).trim(), operationMatcher.group(2), operationMatcher.group(3).trim());
                }
            }
        }
        throw new FilterException("Could not parse filter");
    }

    public boolean matchesEntry(LogEntry entry){
        if(this.left instanceof LogEntry.columnNamesType){
            Object lValue = entry.getValueByKey((LogEntry.columnNamesType) this.left);
            if(this.right instanceof LogEntry.columnNamesType){
                return lValue == entry.getValueByName((String) this.right);
            }else{
                try {
                    return lValue.equals(lValue.getClass().getConstructor(this.right.getClass()).newInstance(this.right));
                } catch (Exception e) {
                    return false;
                }
            }
        }else{
            if(this.right instanceof LogEntry.columnNamesType){
                Object rValue = entry.getValueByName((String) this.right);
                try {
                    return rValue.equals(rValue.getClass().getConstructor(this.left.getClass()).newInstance(this.left));
                } catch (Exception e) {
                    return false;
                }
            }else{
                return this.right.equals(this.left);
            }
        }
    }


    @Override
    public String toString() {
        return (this.inverted ? "INV " : "") + left.toString() + " " + operation.toString() + " " + right.toString();
    }

    public static class FilterException extends Exception{
        public FilterException(String msg) {
            super(msg);
        }
    }
}
