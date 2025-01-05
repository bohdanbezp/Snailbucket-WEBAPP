import os
import json

STATE_FILE_PATH = os.path.join(os.environ.get('HOME', '.'), 'snailbotstate.json')


class State(object):
    def __init__(self):
        self.data = None
        self._ReadState()

    def _ReadState(self):
        try:
            with open(STATE_FILE_PATH, 'r') as f:
                self.data = json.load(f)
        except FileNotFoundError:
            # File not found, creating it with an empty dictionary
            self.data = {}
            self.SaveState()
        except json.JSONDecodeError:
            print('Unable to parse JSON from %s.' % STATE_FILE_PATH)
            print('Please create that file and put valid JSON there.')
            print('Minimal content would be just "{}".')
            raise SystemExit(4)

    @property
    def state(self):
        return self.data

    def SaveState(self):
        tmp_filename = STATE_FILE_PATH + '.tmp'
        with open(tmp_filename, 'w') as f:
            json.dump(self.data, f, indent=2)

        if os.name != 'posix':
            # Windows does not support atomic renames
            os.remove(STATE_FILE_PATH)
        os.rename(tmp_filename, STATE_FILE_PATH)


state = State()
