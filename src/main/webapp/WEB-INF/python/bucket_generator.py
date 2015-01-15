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
    self.max_bucket_size = 8
    self.player_count = len(player_list)
    self.player_list = player_list
    # fetch the names automatically from ratings.fide.com
    self.bucket_names = [ 'Anand', 'Bacrot', 'Carlsen', 'Ding', 'Eljanov',
      'Fressinet', 'Grischuk', 'Harikrishna', 'Ivanchuk', 'Jakovenko',
      'Karjakin', 'Leko', 'Mamedjarov', 'Nakamura' ]
    self.buckets = dict()

  def get_buckets_sizes(self):
    buckets_count = (self.player_count + self.max_bucket_size - 1) / self.max_bucket_size
    if self.player_count % self.max_bucket_size == 0:
      buckets_of_size_8 = buckets_count
      buckets_of_size_7 = 0
    else:
      buckets_of_size_7 = self.max_bucket_size - self.player_count % self.max_bucket_size
      buckets_of_size_8 = buckets_count - buckets_of_size_7
    assert(buckets_of_size_7 * 7 + buckets_of_size_8 * 8 == self.player_count)
    return (buckets_of_size_7, buckets_of_size_8)

  def split_players_to_buckets(self):
    (buckets_of_size_7, buckets_of_size_8) = self.get_buckets_sizes()
    buckets_count = buckets_of_size_7 + buckets_of_size_8
    self.player_list.sort(reverse=True)
    size_of_current_bucket = 0
    bucket_number = -1
    if buckets_of_size_7 > 0:
      buckets_of_size_7 -= 1
      self.buckets[self.bucket_names[buckets_count - 1]] = list()
      for player in self.player_list[-7:]:
        self.buckets[self.bucket_names[buckets_count - 1]].append(player)
      self.player_list = self.player_list[:-7]
    for player in self.player_list:
      if size_of_current_bucket == 0:
        bucket_number += 1
        if buckets_of_size_7 > 0:
          size_of_current_bucket = 7
          buckets_of_size_7 -= 1
        else:
          size_of_current_bucket = 8
        self.buckets[self.bucket_names[bucket_number]] = list()
      size_of_current_bucket -= 1
      self.buckets[self.bucket_names[bucket_number]].append(player)


