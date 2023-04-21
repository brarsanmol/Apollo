package com.octavemc.tablist;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.mojang.authlib.properties.Property;
import com.octavemc.Apollo;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class TablistSlot {

    public static final Property DEFAULT_SKIN = new Property("textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTYwMDM1NjEyMzg4NiwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMDJjYWJhZjI1NWE5M2NjYzczYmQyMTJjMmNjYWVmOGYzZmI5NTZjYjA5M2FmMmQ2YWM3ZmQxMzFkZmQ3YTJhIgogICAgfQogIH0KfQ==",
            "BxnG3B+3fKyL6jJQG/Ioh7SNtfGbzXi9VMH7llPu8clA18yjB3bYtv3c4lo5SKGkgHd2cY0gyGtOOcdu1hv37/V4T6I3udA23NSbNOBsLW5EWQyDkHj/ogdS8nltCgMLlCfOvV+hc/jRdPeUTwRBowaOwMiCGwMLImFm++Snq6ZW24JglkX7dP5SAQ7XMNA56Ge02tlZ1w2IPux72YUcGlMH770oBqJ6xZP1k3v/c67OVqbRQutiBDZHWJ1o5SxNoSOgI9bjZahzge7PB7kRX/Vd4sNnPKr7Wl6PGv/H6BN/4XEXYvnx8exi0mxo06ak/bNTwp0M4ykU1yjGtxZgZ9XJ42NM5FmZutm61RQ4O2En+QgxiNgh5cyuNBVmk6eE+BqGm7YjCb54v8V4+zDlAn3G0u6T9DiVHbWjJXAC8Dx8c3qAhwaCDCyy1pkMBcGkjwoSp3zpfjaaJpNqN01E9R2K+CG8yA/fDQiws9RBkjI6jIJTWtXM3EhHAteq84TeQsgC8fguUR0X+0VyQLiDWDQriTL4ISutosDURDWznLW6ybswRgBoLKyJoJmz/xgfSW0qKRr9byWqp2yH/UOpcOP3M2Ruk81g2xy9BCuY6s/tbAd+o3aYyDKSAPvMaLVr51Zz2LoxXbfa3TDOgTRsLyEHhiDXXRV+bQIpig6w3Qo=");

    private String text;
    private int latency;
    private WrappedGameProfile profile;
    private boolean shouldUpdate;

    protected TablistSlot(@NonNull int index) {
        this.text = " ";
        this.latency = 149;
        this.profile = new WrappedGameProfile(UUID.randomUUID(), "!" + Apollo.getInstance().getTablistKeyGenerator().getNextKey(index));
        this.profile.getProperties().put("textures", WrappedSignedProperty.fromHandle(DEFAULT_SKIN));
        this.shouldUpdate = true;
    }

    public void setText(@NonNull String text) {
        if (this.text.equals(text)) return;
        this.text = text;
        this.shouldUpdate = true;
    }

    public void setLatency(@NonNull int latency) {
        if (this.latency == latency) return;
        this.latency = latency;
        this.shouldUpdate = true;
    }
}
