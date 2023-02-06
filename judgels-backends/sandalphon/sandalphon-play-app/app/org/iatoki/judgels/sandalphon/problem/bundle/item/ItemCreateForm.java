package org.iatoki.judgels.sandalphon.problem.bundle.item;

import play.data.validation.Constraints;

public final class ItemCreateForm {

    @Constraints.Required
    public String itemType;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
