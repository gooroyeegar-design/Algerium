#pragma once
#include <functional>
#include <string>
#include <unordered_map>
#include <vector>
namespace algerium {
struct Event { std::string type; bool bubbles=true; bool default_prevented=false; };
class EventTarget {
 std::unordered_map<std::string,std::vector<std::function<void(Event&)>>> listeners;
public:
 void add_event_listener(const std::string&type,std::function<void(Event&)> cb);
 bool dispatch_event(Event e);
};
}
