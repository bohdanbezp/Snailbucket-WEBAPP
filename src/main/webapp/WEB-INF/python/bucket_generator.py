# Usage:
# generator = BucketGenerator([(1700, "BethanyGrace"),
#     (1900, "PankracyRozumek"),
#     etc...])
# generator.split_players_to_buckets()
# for bucket in generator.buckets():
#   print "Bucket %s" % bucket
#   for player in generator.buckets[bucket]:
#     print player
class BucketGenerator:

    # player_list is list of tuples (rating, "login")
    def __init__(self, player_list):
        self.player_count = len(player_list)
        self.player_list = player_list
        # fetch the names automatically from ratings.fide.com
        self.bucket_names = ['Anand', 'Bacrot', 'Carlsen', 'Ding', 'Eljanov',
                             'Fressinet', 'Grischuk', 'Harikrishna', 'Ivanchuk', 'Jakovenko',
                             'Karjakin', 'Leko', 'Mamedjarov', 'Nakamura']
        self.buckets = dict()

    def get_buckets_sizes(self):
        remaining_players = self.player_count
        buckets_of_size_4 = 0
        buckets_of_size_5 = 0
        buckets_of_size_6 = 0

        while remaining_players > 0:
            if remaining_players >= 6:
                buckets_of_size_6 += 1
                remaining_players -= 6
            elif remaining_players >= 5:
                buckets_of_size_5 += 1
                remaining_players -= 5
            else:
                buckets_of_size_4 += 1
                remaining_players -= 4

        #assert(buckets_of_size_4 * 4 + buckets_of_size_5 * 5 + buckets_of_size_6 * 6 == self.player_count)
        return (buckets_of_size_4, buckets_of_size_5, buckets_of_size_6)

    def split_players_to_buckets(self):
        (buckets_of_size_4, buckets_of_size_5, buckets_of_size_6) = self.get_buckets_sizes()
        self.player_list.sort(reverse=True)
        size_of_current_bucket = 0
        bucket_number = -1

        for player in self.player_list:
            if size_of_current_bucket == 0:
                bucket_number += 1
                if buckets_of_size_4 > 0:
                    size_of_current_bucket = 4
                    buckets_of_size_4 -= 1
                elif buckets_of_size_5 > 0:
                    size_of_current_bucket = 5
                    buckets_of_size_5 -= 1
                else:
                    size_of_current_bucket = 6
                self.buckets[self.bucket_names[bucket_number]] = list()
            size_of_current_bucket -= 1
            self.buckets[self.bucket_names[bucket_number]].append(player)
