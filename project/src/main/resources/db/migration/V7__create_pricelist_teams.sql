CREATE TABLE pricelist_teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    leader_id BIGINT NOT NULL,
    CONSTRAINT fk_pricelist_teams_leader
        FOREIGN KEY (leader_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE team_members (
    team_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    CONSTRAINT pk_team_members PRIMARY KEY (team_id, member_id),
    CONSTRAINT fk_team_members_team
        FOREIGN KEY (team_id)
        REFERENCES pricelist_teams(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_team_members_member
        FOREIGN KEY (member_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_team_members_member_id ON team_members(member_id);