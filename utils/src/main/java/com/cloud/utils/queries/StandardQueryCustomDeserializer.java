package com.cloud.utils.queries;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.cloud.utils.queries.StandardJsonQuery.QueryType;
import com.cloud.utils.queries.json.JsonAuth;
import com.cloud.utils.queries.json.JsonConfirm;
import com.cloud.utils.queries.json.JsonResultAuth;
import com.cloud.utils.queries.json.JsonSendFile;
import com.cloud.utils.queries.json.JsonSimpleMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Десериализатор json - нужен для десериализации обобщенного
 * типа StandardJsonQuery
 * @author prozorova 10.10.2018
 */
public class StandardQueryCustomDeserializer extends JsonDeserializer<StandardJsonQuery> {

	@Override
	public StandardJsonQuery deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		Root root = jp.readValueAs(Root.class);
		StandardJsonQuery jsonQuery = null;
		
		switch (root.queryType) {
			case AUTH_DATA:
				jsonQuery = new JsonAuth(QueryType.AUTH_DATA,
						                 root.standardParams.get(JsonAuth.PARAM_NAME_LOGIN), 
						                 root.standardParams.get(JsonAuth.PARAM_NAME_PASS));
				break;
			case AUTH_RESULT:
				boolean result = Boolean.parseBoolean(root.standardParams.get(JsonResultAuth.PARAM_NAME_ANSWER));
				if (result)
					jsonQuery = new JsonResultAuth(root.paramsWithSet.get(JsonResultAuth.PARAM_NAME_ROOT_FILES));
				else
					jsonQuery = new JsonResultAuth(root.standardParams.get(JsonResultAuth.PARAM_NAME_REJECT_REASON));
				break;
			case SEND_FILE:
				jsonQuery = new JsonSendFile(root.standardParams.get(JsonSendFile.PARAM_NAME_FILENAME), 
						                     Long.parseLong(root.standardParams.get(JsonSendFile.PARAM_NAME_FILESIZE)),
						                     root.standardParams.get(JsonSendFile.PARAM_NAME_CHECKSUM),
						                     root.standardParams.get(JsonSendFile.PARAM_NAME_PATH),
						                     Integer.parseInt(root.standardParams.get(JsonSendFile.PARAM_NAME_PARTS)));
				break;
			case CONFIRMATION:
				jsonQuery = new JsonConfirm(root.paramsWithSet.get(JsonConfirm.PARAM_NAME_DIR_FILES));
				break;
			case REG_DATA:
				jsonQuery = new JsonAuth(QueryType.REG_DATA,
						                 root.standardParams.get(JsonAuth.PARAM_NAME_LOGIN), 
		                                 root.standardParams.get(JsonAuth.PARAM_NAME_PASS));
				break;
			case MESSAGE:
				jsonQuery = new JsonSimpleMessage(root.standardParams.get(JsonSimpleMessage.PARAM_NAME_MESSAGE),
						                          Boolean.parseBoolean(root.standardParams.get(JsonSimpleMessage.PARAM_NAME_DISCONNECT)));
			default:
				break;
		}

        return jsonQuery;
	}
	
	// Аналогичен структуре StandardJsonQuery
	private static class Root {
		public StandardJsonQuery.QueryType queryType;
		public Map<String, String> standardParams;
		public Map<String, Set<String>> paramsWithSet;
    }

}
