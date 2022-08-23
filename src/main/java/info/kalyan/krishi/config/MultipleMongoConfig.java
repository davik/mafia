// package info.kalyan.krishi.config;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.mongo.MongoProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.mongodb.MongoDbFactory;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

// import com.mongodb.client.MongoClient;

// @Configuration
// public class MultipleMongoConfig {

// @Primary
// @Bean(name = "primary")
// @ConfigurationProperties(prefix = "spring.data.mongodb.prim")
// public MongoProperties getPrimary() {
// return new MongoProperties();
// }

// @Bean(name = "secondary")
// @ConfigurationProperties(prefix = "spring.data.mongodb.seco")
// public MongoProperties getSecondary() {
// return new MongoProperties();
// }

// @Primary
// @Bean(name = "primaryMongoTemplate")
// public MongoTemplate primaryMongoTemplate() throws Exception {
// return new MongoTemplate(primaryFactory(getPrimary()));
// }

// @Bean(name = "secondaryMongoTemplate")
// public MongoTemplate secondaryMongoTemplate() throws Exception {
// return new MongoTemplate(secondaryFactory(getSecondary()));
// }

// @Bean
// @Primary
// public MongoDbFactory primaryFactory(final MongoProperties mongo) throws
// Exception {
// return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(),
// mongo.getPort()),
// mongo.getDatabase());
// }

// @Bean
// public MongoDbFactory secondaryFactory(final MongoProperties mongo) throws
// Exception {
// return new SimpleMongoDbFactory(new MongoClient(mongo.getHost(),
// mongo.getPort()),
// mongo.getDatabase());
// }
// }