# Takes list of 7 or 8 objects to pair.
# Returns dict of bucket_number->list of pairings.
def generate(player_list):
  # http://fr.wikipedia.org/wiki/Table_de_Berger#7_.26_8_joueurs
  berger8 = {
      1 : [ (1,8), (2,7), (3,6), (4,5)],
      2 : [ (8,5), (6,4), (7,3), (1,2)],
      3 : [ (2,8), (3,1), (4,7), (5,6)],
      4 : [ (8,6), (7,5), (1,4), (2,3)],
      5 : [ (3,8), (4,2), (5,1), (6,7)],
      6 : [ (8,7), (1,6), (2,5), (3,4)],
      7 : [ (4,8), (5,3), (6,2), (7,1)]}

  # Perhaps add a bye.
  # Adding bye must occur after shuffling, because BYE must be at the last 8th
  # position. Otherwise some players would have 4 white and 2 black, and some
  # players 2 white and 4 black games in 7-person tournament.
  if len(player_list) == 7:
    player_list.append('BYE')

  berger8_with_players = dict()
  for round in berger8:
    berger8_with_players[round] = list()
    for game in berger8[round]:
      berger8_with_players[round].append((
            player_list[game[0] - 1], player_list[game[1] - 1]))

  return berger8_with_players



