import operator

class PlayerRecord:
  def __init__(self, player, rating, forfeits):
    self.player = player
    self.rating = -rating
    self.games = 0
    self.points = 0
    self.forfeits = -forfeits
    self.won = 0
    self.white = 0
    self.hth = 0
    self.opponents = dict()

  def to_tuple(self):
    return (self.player, self.games, self.points, self.hth, self.won,
        -self.white, -self.rating)

def adjust_hth(player_records):
  players = list()
  for record in player_records:
    players.append(record.player)

  for record in player_records:
    for opponent in record.opponents:
      if opponent in players:
        record.hth += record.opponents[opponent]

def normalize_score(score):
  if score == '-':
    return 0
  if score == '+':
    return 1
  return score


# players is a list of (player, rating) tuples
# matches is a list of (player1, player2, score1, score2) tuples
# player is string or any object representing player
# score1 and score2 should be 1, 0.5 or 0.
# returns sorted list of tuples of (player, points, hth points, won, games with white)
#
# use this function for season == 1
def calculate_standings(players, matches):

  records = dict()
  for (player, rating) in players:
    records[player] = PlayerRecord(player, rating, 0)
  for (player1, player2, score1, score2) in matches:
    score1 = normalize_score(score1)
    score2 = normalize_score(score2)

    records[player1].games += 1
    records[player2].games += 1
    records[player1].points += score1
    records[player2].points += score2
    if score1 == 1.0:
      records[player1].won += 1
    if score2 == 1.0:
      records[player2].won += 1
    records[player1].white -= 1
    if player2 not in records[player1].opponents:
      records[player1].opponents[player2] = 0
    if player1 not in records[player2].opponents:
      records[player2].opponents[player1] = 0
    records[player1].opponents[player2] += score1
    records[player2].opponents[player1] += score2

  records_list = list(records.values())
  records_list.sort(key=operator.attrgetter('points'), reverse=True)

  current_points = 123456789
  current_bucket = list()
  for record in records_list:
    if record.points == current_points:
      current_bucket.append(record)
    else:
      if len(current_bucket) >= 2:
        adjust_hth(current_bucket)
      current_points = record.points
      current_bucket = [record]
  if len(current_bucket) >= 2:
    adjust_hth(current_bucket)

  records_list.sort(
      key=operator.attrgetter('points', 'hth', 'won', 'white', 'rating'),
      reverse=True)

  return [player.to_tuple() for player in records_list]


# players is a list of (player, rating, forfeits) tuples
# matches is a list of (player1, player2, score1, score2) tuples
# player is string or any object representing player
# score1 and score2 should be 1, 0.5 or 0.
# returns sorted list of tuples of (player, points, hth points, forfeits, won, games with white)
#
# use this function for season >= 2
def calculate_standings_with_forfeits(players, matches, season):

  records = dict()
  for (player, rating, forfeits) in players:
    records[player] = PlayerRecord(player, rating, forfeits)
  for (player1, player2, score1, score2) in matches:
    score1 = normalize_score(score1)
    score2 = normalize_score(score2)

    records[player1].games += 1
    records[player2].games += 1
    records[player1].points += score1
    records[player2].points += score2
    if score1 == 1.0:
      records[player1].won += 1
    if score2 == 1.0:
      records[player2].won += 1
    records[player1].white -= 1
    if player2 not in records[player1].opponents:
      records[player1].opponents[player2] = 0
    if player1 not in records[player2].opponents:
      records[player2].opponents[player1] = 0
    records[player1].opponents[player2] += score1
    records[player2].opponents[player1] += score2

  records_list = list(records.values())
  records_list.sort(key=operator.attrgetter('points'), reverse=True)

  current_points = 123456789
  current_bucket = list()
  for record in records_list:
    if record.points == current_points:
      current_bucket.append(record)
    else:
      if len(current_bucket) >= 2:
        adjust_hth(current_bucket)
      current_points = record.points
      current_bucket = [record]
  if len(current_bucket) >= 2:
    adjust_hth(current_bucket)

  records_list.sort(
      key=operator.attrgetter('points', 'hth', 'forfeits', 'won', 'white', 'rating'),
      reverse=True)

  return [player.to_tuple() for player in records_list]

