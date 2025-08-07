package fr.robotv2.placeholderannotationlib.api;

import fr.robotv2.placeholderannotationlib.impl.PlaceholderActorImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface PlaceholderActor {

    @Nullable
    OfflinePlayer getPlayer();

    @Nullable
    Player getOnlinePlayer();

    @Nonnull
    Player requireOnlinePlayer();

    static PlaceholderActor of(OfflinePlayer player) {
        return new PlaceholderActorImpl(player);
    }

    static PlaceholderActor empty() {
        return new PlaceholderActorImpl(null);
    }
}
