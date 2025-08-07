package fr.robotv2.placeholderannotationlib.impl;

import fr.robotv2.placeholderannotationlib.api.PlaceholderActor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class PlaceholderActorImpl implements PlaceholderActor {

    private final OfflinePlayer issuer;

    public PlaceholderActorImpl(OfflinePlayer issuer) {
        this.issuer = issuer;
    }

    @Nullable
    @Override
    public OfflinePlayer getPlayer() {
        return issuer;
    }

    @Nullable
    @Override
    public Player getOnlinePlayer() {
        return (issuer instanceof Player && issuer.isOnline()) ? (Player) issuer : null;
    }

    @Nonnull
    @Override
    public Player requireOnlinePlayer() {
        return Objects.requireNonNull(getOnlinePlayer(), "An online player is required for this placeholder.");
    }
}
