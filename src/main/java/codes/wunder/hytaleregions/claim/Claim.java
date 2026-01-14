package codes.wunder.hytaleregions.claim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Claim {

    private final String owner;
    private final Set<String> users = new HashSet<>();

    public Claim(String owner) {
        this.owner = owner;
    }

    public boolean isOwner(String id) {
        return owner.equals(id);
    }

    public boolean isMember(String id) {
        return isOwner(id) || users.contains(id);
    }

    public void addUser(String id) {
        users.add(id);
    }

    public void removeUser(String id) {
        users.remove(id);
    }

    public String owner() {
        return owner;
    }

    public Set<String> usersView() {
        return Collections.unmodifiableSet(users);
    }
}

