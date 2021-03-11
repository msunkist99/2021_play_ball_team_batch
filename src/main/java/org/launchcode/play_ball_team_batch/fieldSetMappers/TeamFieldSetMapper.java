package org.launchcode.play_ball_team_batch.fieldSetMappers;

import org.launchcode.play_ball_team_batch.models.Team;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class TeamFieldSetMapper implements FieldSetMapper<Team> {


    @Override
    public Team mapFieldSet(FieldSet fieldSet) throws BindException {
        return new Team(
                fieldSet.readString(0),         // team id
                fieldSet.readString(1),         // league id
                fieldSet.readString(2),         // team city
                fieldSet.readString(3)        // team name
        );
    }
}
