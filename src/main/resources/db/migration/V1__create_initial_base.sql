CREATE TABLE IF NOT EXISTS proposal(
  id UUID PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS voting_session(
    id UUID PRIMARY KEY,
    proposal_id UUID NOT NULL references proposal(id),
    opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    closes_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS vote(
  id UUID PRIMARY KEY,
  proposal_id UUID NOT NULL REFERENCES proposal(id),
  associate_id UUID NOT NULL,
  voting_session_id UUID NOT NULL REFERENCES voting_session(id),
  vote BOOLEAN NOT NULL,
  CONSTRAINT uk_vote_proposal_associate unique (proposal_id, associate_id)
);

CREATE INDEX IF NOT EXISTS vote_proposal_idx ON vote(proposal_id);
CREATE INDEX IF NOT EXISTS vote_proposal_vote_idx on vote(proposal_id, vote);
