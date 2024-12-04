package com.example.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "message_ref")
/*
* 使得一个类的对象能够被 序列化
* 即将对象的状态转换为字节流，以便将对象存储到磁盘、传输到网络上，或者保存到数据库中
* 只有实现了 Serializable 接口的类的对象，才能进行序列化和反序列化。
* 序列化：将对象转化为字节流的过程，可以将对象保存到文件中，或者通过网络传输。
* 反序列化：将字节流还原为对象的过程，通常用于接收网络传输的数据或从文件中读取数据。
* */
public class MessageRefEntity implements Serializable {
    @Id
    private String _id;

    @Indexed
    private String messageId;

    @Indexed
    private Integer receiverId;

    @Indexed
    private Boolean readFlag;

    @Indexed
    private Boolean lastFlag;
}
