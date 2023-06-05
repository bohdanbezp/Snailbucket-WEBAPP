# Takes list of 7 or 8 objects to pair.
# Returns dict of bucket_number->list of pairings.
def generate(player_list):
    berger_tables = {
        4: {
            1: [(1, 4), (2, 3)],
            2: [(4, 3), (1, 2)],
            3: [(2, 4), (3, 1)],
            4: [(4, 1), (3, 2)],
            5: [(1, 3), (2, 4)],
            6: [(3, 4), (1, 2)]
        },
        5: {
            1: [(1, 5), (2, 4), (3, 0)],
            2: [(5, 3), (4, 2), (1, 0)],
            3: [(2, 5), (3, 1), (4, 0)],
            4: [(5, 4), (1, 3), (2, 0)]
        },
        6: {
            1: [(1, 6), (2, 5), (3, 4)],
            2: [(6, 4), (5, 3), (1, 2)],
            3: [(2, 6), (3, 1), (4, 5)],
            4: [(6, 5), (1, 4), (2, 3)],
            5: [(3, 6), (4, 2), (5, 1)]
        }
    }

    num_players = len(player_list)
    if num_players == 5:
        player_list.append('BYE')
        num_players += 1

    if num_players not in berger_tables:
        return {}  # Return an empty dictionary if the number of players is not supported

    berger_table = berger_tables[num_players]

    berger_with_players = dict()
    for round in berger_table:
        berger_with_players[round] = list()
        for game in berger_table[round]:
            if game[0] != 0 and game[1] != 0:
                berger_with_players[round].append((
                    player_list[game[0] - 1], player_list[game[1] - 1]))

    return berger_with_players
