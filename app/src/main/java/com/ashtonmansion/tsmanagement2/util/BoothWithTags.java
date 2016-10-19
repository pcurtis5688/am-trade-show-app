package com.ashtonmansion.tsmanagement2.util;

import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v3.inventory.Tag;


/**
 * Created by paul curtis
 * (pcurtis5688@gmail.com)
 * on 10/13/2016.
 */
public class BoothWithTags {
    /**
     * Created by paul curtis
     * (pcurtis5688@gmail.com)
     * on 10/13/2016.
     */
    private Item booth;
    private Tag sizeTag;
    private Tag areaTag;
    private Tag typeTag;
    private String unformattedSize;
    private String unformattedArea;
    private String unformattedType;

    public BoothWithTags(Item booth) {
        this.booth = booth;
        for (Tag currentTag : booth.getTags()) {
            if (!currentTag.getName().contains(" [Show]")) {
                if (currentTag.getName().substring(0, 4).equalsIgnoreCase("size")) {
                    sizeTag = currentTag;
                    unformattedSize = GlobalUtils.getUnformattedTagName(sizeTag.getName(), "Size");
                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("area")) {
                    areaTag = currentTag;
                    unformattedArea = GlobalUtils.getUnformattedTagName(areaTag.getName(), "Area");
                } else if (currentTag.getName().substring(0, 4).equalsIgnoreCase("type")) {
                    typeTag = currentTag;
                    unformattedType = GlobalUtils.getUnformattedTagName(typeTag.getName(), "Type");
                }
            }
        }
    }

    public Item getBooth() {
        return booth;
    }

    public Tag getSizeTag() {
        return sizeTag;
    }

    public void setSizeTag(Tag sizeTag) {
        this.sizeTag = sizeTag;
    }

    public Tag getAreaTag() {
        return areaTag;
    }

    public void setAreaTag(Tag areaTag) {
        this.areaTag = areaTag;
    }

    public Tag getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(Tag typeTag) {
        this.typeTag = typeTag;
    }

    public String getUnformattedSize() {
        return unformattedSize;
    }

    public String getUnformattedArea() {
        return unformattedArea;
    }

    public String getUnformattedType() {
        return unformattedType;
    }
}

