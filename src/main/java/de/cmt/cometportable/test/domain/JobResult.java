package de.cmt.cometportable.test.domain;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


public class JobResult implements Serializable {

    private static final long serialVersionUID = -5009274691165427644L;

    private Long id;

    private ResultType type;

    private List<JobResultItem> items;

    public JobResult() {
        this.items = new ArrayList<>();
        this.type = ResultType.UNKNOWN;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResultType getType() {
        return type;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    public List<JobResultItem> getItems() {
        return items;
    }

    public void setItems(List<JobResultItem> items) {
        this.items = items;
    }

    public void addItem(JobResultItem item) {
        this.items.add(item);
        //regenerate type
        if(this.getType() == ResultType.UNKNOWN || this.getType() == ResultType.VALID) {
            //update anytime
            this.setType(item.getType());
        }
    }

}


