package com.timebank.mapper;

import com.timebank.domain.Video;
import com.timebank.domain.VideoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VideoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    long countByExample(VideoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int deleteByExample(VideoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String videoGuid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int insert(Video record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int insertSelective(Video record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    List<Video> selectByExample(VideoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    Video selectByPrimaryKey(String videoGuid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") Video record, @Param("example") VideoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") Video record, @Param("example") VideoExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(Video record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dbo.VIDEO
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(Video record);
}