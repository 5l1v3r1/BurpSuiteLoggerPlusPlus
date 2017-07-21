package burp.filter;

import com.google.gson.JsonObject;

import java.awt.*;
import java.util.UUID;

/**
 * Created by corey on 19/07/17.
 */
public class ColorFilter {
    private UUID uid;
    private String name;
    private Filter filter;
    private String filterString;
    private Color backgroundColor;
    private Color foregroundColor;
    private boolean enabled;
    private boolean modified;

    public ColorFilter(){
        this.uid = UUID.randomUUID();
        this.enabled = true;
    }

    public UUID getUid() {
        return uid;
    }

    public void setBackgroundColor(Color backgroundColor){
        this.backgroundColor = backgroundColor;
        this.modified = true;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getForegroundColor() {return foregroundColor;}

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
        modified = true;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        modified = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        modified = true;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public boolean equals(Object obj){
        if(obj instanceof ColorFilter){
            return ((ColorFilter) obj).getUid().equals(this.uid);
        }else{
            return super.equals(obj);
        }
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }


}
