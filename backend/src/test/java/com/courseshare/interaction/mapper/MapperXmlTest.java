package com.courseshare.interaction.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperXmlTest {

    @Test
    void interactionMapperXmlParses() throws Exception {
        List<String> resources = List.of(
                "mapper/interaction/ResourceFavoriteMapper.xml",
                "mapper/interaction/ResourceReviewMapper.xml",
                "mapper/interaction/ReviewLikeMapper.xml",
                "mapper/resource/CmsArticleMapper.xml"
        );
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        for (String resource : resources) {
            try (InputStream input = Resources.getResourceAsStream(resource)) {
                XMLMapperBuilder builder = new XMLMapperBuilder(
                        input,
                        configuration,
                        resource,
                        configuration.getSqlFragments()
                );
                builder.parse();
            }
        }
        assertTrue(configuration.hasStatement(
                "com.courseshare.interaction.mapper.ResourceReviewMapper.selectPublicPage"
        ));
        assertTrue(configuration.hasStatement(
                "com.courseshare.resource.mapper.CmsArticleMapper.selectDiscoverPage"
        ));
    }
}
