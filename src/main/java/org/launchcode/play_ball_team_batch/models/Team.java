package org.launchcode.play_ball_team_batch.models;

public class Team {
    private String id;
    private String leagueId;
    private String city;
    private String name;

    public Team(String id, String leagueId, String city, String name) {
        this.id = id;
        this.leagueId = leagueId;
        this.name = name;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }
}
