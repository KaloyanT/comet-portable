package de.cmt.cometportable.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CometShellUtil {

    public String listExportedJobs(List<ObjectNode> jobs) {

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < jobs.size(); i++) {

            ObjectNode node = jobs.get(i);

            if(node.has("id")) {
                stringBuilder.append("Job." + node.get("id") + (i == jobs.size() - 1 ? "" : "\n"));
            }
        }

        return stringBuilder.toString();
    }
}
