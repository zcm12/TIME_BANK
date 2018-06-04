package com.timebank.controller.wbw;

import com.timebank.domain.Community;
import com.timebank.domain.Type;
import com.timebank.mapper.CommunityMapper;
import com.timebank.mapper.TypeMapper;

import static java.util.UUID.randomUUID;

/**
 * 手动插入部分表格数据
 */
public class InsertTableByHand {
    /**
     * 生成TYPE表中数据
     * @param typeMapper
     */
    public static void insertTypeTable(TypeMapper typeMapper){
        Type type1 = new Type();
        type1.setTypeGuid(randomUUID().toString());
        type1.setTypeGroupId(1);
        type1.setTypeTitle("男");
        typeMapper.insert(type1);

        Type type2= new Type();
        type2.setTypeGuid(randomUUID().toString());
        type2.setTypeGroupId(1);
        type2.setTypeTitle("女");
        typeMapper.insert(type2);

        Type type3 = new Type();
        type3.setTypeGuid(randomUUID().toString());
        type3.setTypeGroupId(2);
        type3.setTypeTitle("账号正常");
        typeMapper.insert(type3);

        Type type4 = new Type();
        type4.setTypeGuid(randomUUID().toString());
        type4.setTypeGroupId(2);
        type4.setTypeTitle("账号暂停");
        typeMapper.insert(type4);

        Type type5 = new Type();
        type5.setTypeGuid(randomUUID().toString());
        type5.setTypeGroupId(3);
        type5.setTypeTitle("待处理");
        typeMapper.insert(type5);

        Type type6 = new Type();
        type6.setTypeGuid(randomUUID().toString());
        type6.setTypeGroupId(3);
        type6.setTypeTitle("处理中");
        typeMapper.insert(type6);

        Type type7 = new Type();
        type7.setTypeGuid(randomUUID().toString());
        type7.setTypeGroupId(3);
        type7.setTypeTitle("撤销");
        typeMapper.insert(type7);

        Type type8 = new Type();
        type8.setTypeGuid(randomUUID().toString());
        type8.setTypeGroupId(3);
        type8.setTypeTitle("已完成");
        typeMapper.insert(type8);

        Type type9 = new Type();
        type9.setTypeGuid(randomUUID().toString());
        type9.setTypeGroupId(4);
        type9.setTypeTitle("聊天");
        typeMapper.insert(type9);

        Type type10 = new Type();
        type10.setTypeGuid(randomUUID().toString());
        type10.setTypeGroupId(4);
        type10.setTypeTitle("体力活");
        typeMapper.insert(type10);

        Type type11 = new Type();
        type11.setTypeGuid(randomUUID().toString());
        type11.setTypeGroupId(5);
        type11.setTypeTitle("紧急");
        typeMapper.insert(type11);

        Type type12 = new Type();
        type12.setTypeGuid(randomUUID().toString());
        type12.setTypeGroupId(5);
        type12.setTypeTitle("不急");
        typeMapper.insert(type12);

        Type type13 = new Type();
        type13.setTypeGuid(randomUUID().toString());
        type13.setTypeGroupId(6);
        type13.setTypeTitle("待处理");
        typeMapper.insert(type13);

        Type type14 = new Type();
        type14.setTypeGuid(randomUUID().toString());
        type14.setTypeGroupId(6);
        type14.setTypeTitle("已完成");
        typeMapper.insert(type14);

        Type type15 = new Type();
        type15.setTypeGuid(randomUUID().toString());
        type15.setTypeGroupId(6);
        type15.setTypeTitle("超时");
        typeMapper.insert(type15);

        Type type16 = new Type();
        type16.setTypeGuid(randomUUID().toString());
        type16.setTypeGroupId(6);
        type16.setTypeTitle("拒绝");
        typeMapper.insert(type16);

        Type type17 = new Type();
        type17.setTypeGuid(randomUUID().toString());
        type17.setTypeGroupId(6);
        type17.setTypeTitle("失败");
        typeMapper.insert(type17);

        Type type18 = new Type();
        type18.setTypeGuid(randomUUID().toString());
        type18.setTypeGroupId(6);
        type18.setTypeTitle("撤销");
        typeMapper.insert(type18);
    }

    /**
     * 生成COMMUNITY表中数据
     * @param communityMapper
     */
    public static void insertCommunityTable(CommunityMapper communityMapper) {
        Community community1 = new Community();
        community1.setCommGuid(randomUUID().toString());
        community1.setCommTitle("福源小区");
        communityMapper.insert(community1);

        Community community2 = new Community();
        community2.setCommGuid(randomUUID().toString());
        community2.setCommTitle("山南新区");
        communityMapper.insert(community2);

        Community community3 = new Community();
        community3.setCommGuid(randomUUID().toString());
        community3.setCommTitle("西关小区");
        communityMapper.insert(community3);

        Community community4 = new Community();
        community4.setCommGuid(randomUUID().toString());
        community4.setCommTitle("盛德小区");
        communityMapper.insert(community4);

        Community community5 = new Community();
        community5.setCommGuid(randomUUID().toString());
        community5.setCommTitle("新界小区");
        communityMapper.insert(community5);
    }
}
