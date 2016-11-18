package com.gome.im.dispatcher.mongo;

import com.gome.im.dispatcher.model.RpcServerModel;
import com.gome.im.dispatcher.model.ServerModel;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class ServerDao extends BaseDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String COLL_NAME = "t_server";

    public void saveOrUpdateServer(ServerModel server) {
        try {
            MongoCollection<Document> coll = this.getCollection(dbName, COLL_NAME);
            Document doc = new Document();
            if (server.getType() > 0) {
                doc.put("type", server.getType());
            }
            if (server.getCmd() != null) {
                doc.put("cmd", server.getCmd());
            }
            if (server.getUpdateTime() > 0) {
                doc.put("updateTime", server.getUpdateTime());
            }
            doc.put("status", server.getStatus());
            Bson filter = Filters.eq("ipPort", server.getIpPort());
            coll.findOneAndUpdate(filter, new Document("$set", doc), new FindOneAndUpdateOptions().upsert(true));
        } catch (Exception e) {
            log.error("mongodb saveOrUpdateServer error:{},server ipPort:{},type:{}", e, server.getIpPort(), server.getType());
        }
    }

    public void saveOrUpdateRPCServer(RpcServerModel server) {
        try {
            MongoCollection<Document> coll = this.getCollection(dbName, COLL_NAME);
            Document doc = new Document();
            if (server.getType() > 0) {
                doc.put("type", server.getType());
            }
            if (server.getCmd() != null) {
                doc.put("cmd", server.getCmd());
            }
            if (server.getUpdateTime() > 0) {
                doc.put("updateTime", server.getUpdateTime());
            }
            doc.put("status", server.getStatus());
            doc.put("weight",server.getWeight());

            Bson filter = Filters.eq("ipPort", server.getIpPort());
            coll.findOneAndUpdate(filter, new Document("$set", doc), new FindOneAndUpdateOptions().upsert(true));
        } catch (Exception e) {
            log.error("mongodb saveOrUpdateRPCServer error:{},server ipPort:{},type:{}", e, server.getIpPort(), server.getType());
        }
    }

    public void deleteServer(String ipPort) {
        try {
            MongoCollection<Document> coll = this.getCollection(dbName, COLL_NAME);
            Bson filter = Filters.eq("ipPort", ipPort);
            coll.deleteMany(filter);
        } catch (Exception e) {
            //e.printStackTrace();
            log.error("mongodb deleteServer error:{},server ipPort:{}", e, ipPort);
        }
    }

}
