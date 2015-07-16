
import os
import os.path
import json

STATE_FILE_PATH = os.path.join(os.environ.get('HOME', '.'),
                               'snailbotstate.json')


# TODO(crem): For now only bot writes to that file, so no locking is
#             implemented. Implement locking if needed.
class State(object):
    def __init__(self):
        self.data = None
        self._ReadState()


    def _ReadState(self):
        try:
            f = open(STATE_FILE_PATH, 'r')
        except:
            # File not found, creating.
            self.date = {}
            self.SaveState()

        try:
            self.data = json.load(f)
            f.close()
        except:
            print 'Unable to parse json from %s.' % STATE_FILE_PATH;
            print 'Please create that file and put valid JSON there.'
            print 'Minimal content would be just "{}".'
            raise SystemExit(4)


    @property
    def state(self):
        return self.data


    def SaveState(self):
        tmp_filename = STATE_FILE_PATH + '.tmp'
        f = open(tmp_filename, 'w')
        f.write(json.dumps(self.data, indent=2))
        f.close()
        if os.name != 'posix':
            # Windows doesn't atomic renames
            os.remove(STATE_FILE_PATH)
        os.rename(tmp_filename, STATE_FILE_PATH)


state = State()
