package com.octavemc.guice.providers;

import com.google.inject.Provider;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.claim.Subclaim;
import com.octavemc.faction.type.*;
import com.octavemc.user.User;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.bson.UuidRepresentation;

public class DatastoreProvider implements Provider<Datastore> {

    @Override
    public Datastore get() {
        var datastore = Morphia
                .createDatastore(MongoClients
                                .create(MongoClientSettings
                                        .builder()
                                        .uuidRepresentation(UuidRepresentation.JAVA_LEGACY)
                                        .build()),
                        "octave");
        /*datastore.getMapper().mapPackage("com.octavemc");*/
        datastore.getMapper().map(
                User.class,
                Claim.class,
                Subclaim.class,
                Faction.class,
                PlayerFaction.class,
                RoadFaction.class,
                EndPortalFaction.class,
                ClaimableFaction.class,
                SpawnFaction.class,
                WarzoneFaction.class,
                WildernessFaction.class);
        datastore.ensureIndexes();
        return datastore;
    }
}
