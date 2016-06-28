/**
 * Author: Koushik Sen
 * Created on: 2/5/16.
 */

module.exports = (function () {

    function extend(Child, Parent) {
        Child.prototype = inherit(Parent.prototype)
        Child.prototype.constructor = Child
        Child.parent = Parent.prototype
    }

    function inherit(proto) {
        function F() {
        }

        F.prototype = proto
        return new F
    }

    function TrieNode() {
        this.isSet = false;
        this.children = null;
    }

    TrieNode.prototype.get = function (c) {
        var ret;
        if (this.children == null)
            return null;
        else if ((ret = this.children[c]) === undefined)
            return null;
        else
            return ret;
    };

    TrieNode.prototype.getOrCreate = function (c) {
        var ret;
        if (this.children == null) {
            this.children = new Object(null);
        }
        if ((ret = this.children[c]) == undefined) {
            ret = new TrieNode();
            this.children[c] = ret;
        }
        return ret;
    };


    function StringTokenScanner(ID, escapeChar) {
        this.escapeChar = escapeChar;
        this.trie = new TrieNode();
        if (this.escapeChar) {
            this.addStringAux(ID, this.escapeChar);
            this.addStringAux(ID - 1, this.escapeChar + this.escapeChar, this.escapeChar);
        }
    }

    StringTokenScanner.EOF = -1;

    StringTokenScanner.prototype.setStream = function (stream) {
        this.stream = stream;
        this.tokenIndex = 0;
        this.charIndex = 0;
        this.availableChars = [];
    };

    StringTokenScanner.prototype.addString = function (ID, lexeme, actual) {
        this.addStringAux(ID, lexeme, actual);
        if (this.escapeChar) {
            this.addStringAux(ID - 1, this.escapeChar + lexeme, lexeme);
        }
    }

    StringTokenScanner.prototype.addStringAux = function (ID, lexeme, actual) {
        var len = lexeme.length;
        var current = this.trie;

        for (var i = 0; i < len; i++) {
            var c = lexeme.charCodeAt(i);
            current = current.getOrCreate(c);
        }
        current.ID = ID;
        if (actual !== undefined)
            current.s = actual;
        else
            current.s = lexeme;
        current.isSet = true;
        return ID;
    };


    StringTokenScanner.prototype.readChar = function () {
        var inp;
        if (this.availableChars.length === 0) {
            inp = this.stream.charCodeAt(this.charIndex);
            if (inp !== inp) inp = StringTokenScanner.EOF;
            this.charIndex++;
        } else {
            inp = this.availableChars.pop();
        }
        return inp;
    };

    StringTokenScanner.prototype.pushChar = function (c) {
        this.availableChars.push(c);
    };

    StringTokenScanner.prototype.nextToken = function () {
        var inp;
        this.tokenIndex++;
        inp = this.readChar();
        if (inp === StringTokenScanner.EOF) {
            this.lexeme = "";
            return this.token = inp;
        }
        var oldCharIndex = this.charIndex;
        var firstInp = inp;
        var prev = null;
        var ret = this.trie.get(inp);
        while (ret != null) {
            inp = this.readChar();
            prev = ret;
            ret = ret.get(inp);
        }
        if (prev != null && prev.isSet) {
            this.pushChar(inp);
            this.lexeme = prev.s;
            return this.token = prev.ID;
        } else {
            this.charIndex = oldCharIndex;
            this.lexeme = String.fromCharCode(firstInp);
            return this.token = firstInp;
        }
    };

    StringTokenScanner.prototype.scannedPrefix = function () {
        return this.stream.substring(0, this.charIndex - 1);
    };

    function UniversalASTWithHole(top, parent, i) {
        this.parent = parent;
        this.top = top;
        this.i = i;
    }

    UniversalASTWithHole.prototype.toString = function () {
        if (this.parent) {
            return this.top + " - " + this.parent.children[this.i];
        } else {
            return "Empty";
        }
    }

    function UniversalAST() {
        this.children = [];
    }

    UniversalAST.prototype.toString = function () {
        var ret = "";
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child instanceof UniversalAST) {
                ret = ret + UniversalAST.LBS;
                ret = ret + child.toString();
                ret = ret + UniversalAST.RBS;
            } else {
                ret = ret + universalASTEscape(child + "");
            }
        }
        return ret;
    };

    UniversalAST.prototype.toSourceString = function () {
        var ret = "";
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child instanceof UniversalAST) {
                ret = ret + child.toSourceString();
            } else {
                ret = ret + (child + "");
            }
        }
        return ret;
    };

    UniversalAST.prototype.toSourceStringClipped = function () {
        var ret = "";
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child instanceof UniversalAST) {
                var tmp = child.toSourceStringClipped();
                ret = ret + tmp.substring(tmp.indexOf(' ') + 1);
            } else {
                ret = ret + (child + "");
            }
        }
        return ret;
    };

    UniversalAST.prototype.addChild = function (child) {
        this.children.push(child);
    };

    UniversalAST.prototype.matchContext = function (patternStar, ret, top, parent, index) {
        var oldlen = ret.length;
        ret.push(new UniversalASTWithHole(top, parent, index));
        var ret2 = this.matchExact(patternStar, ret);
        if (ret2) {
            return ret2;
        }
        ret.length = oldlen;

        for (var i = 0; i < this.children.length; i++) {
            if (this.children[i] instanceof UniversalAST) {
                ret2 = this.children[i].matchContext(patternStar, ret, top, this, i);
                if (ret2) {
                    return ret2;
                }
                ret.length = oldlen;
            }
        }
        return false;
    }

    UniversalAST.prototype.matchStar = function (pattern, ret, k) {
        var ktmp = k;
        do {
            k = ktmp;
            ktmp = this.matchList(pattern, ret, k);
        } while (ktmp !== -1);
        return k;
    }

    UniversalAST.prototype.matchAlternation = function (pattern, ret, k) {
        for (var i = 0; i < pattern.children.length; i++) {
            var o = pattern.children[i];
            var ktmp = this.matchList(o, ret, k);
            if (ktmp !== -1) {
                return ktmp;
            }
        }
        return -1;
    }

    UniversalAST.prototype.matchList = function (pattern, ret, k) {
        var oldlen = ret.length, flag;

        for (var i = 0; i < pattern.children.length && k !== -1; i++) {
            var o = pattern.children[i];
            if (o instanceof RegExp) {
                var str;
                if (this.children[k] instanceof UniversalAST || k >= this.children.length) {
                    k--;
                    str = "";
                } else {
                    str = this.children[k] + "";
                }
                var m = o.exec(str);
                if (m !== null) {
                    for (var j = 1; j < m.length; j++) {
                        ret.push(m[j]);
                    }
                    k++;
                } else {
                    k = -1;
                }
            } else if (o instanceof TRegexAST) {
                if (o.isStar) {
                    k = this.matchStar(o, ret, k);
                } else if (o.isAlternation) {
                    k = this.matchAlternation(o, ret, k);
                } else if (this.children[k] instanceof UniversalAST) {
                    if (o.isAt) {
                        ret.push(this.children[k]);
                        k++;
                    } else if (o.isContext) {
                        flag = this.children[k].matchContext(o, ret, this.children[k]);
                        if (flag) k++; else k = -1;
                    } else {
                        flag = this.children[k].matchExact(o, ret);
                        if (flag) k++; else k = -1;
                    }
                } else {
                    k = -1;
                }
            } else if (o !== this.children[k]) {
                k = -1;
            }
        }
        if (k === -1) {
            ret.length = oldlen;
        }
        return k;
    }

    UniversalAST.prototype.matchExact = function (pattern, ret) {
        var k = 0;
        var oldlen = ret.length;

        k = this.matchList(pattern, ret, k);
        if (this.children.length !== k) {
            ret.length = oldlen;
            return false;
        }
        return true;
    }

    UniversalAST.prototype.matches = function (pattern) {
        var ret = [undefined];
        if (this.matchExact(pattern, ret)) {
            return ret;
        } else {
            return null;
        }
    }


    UniversalAST.prototype.replaceInString = function (template, subs, stack) {
        var sb = "";
        var len = template.length;
        var prev = 0, c;
        for (var i = 0; i < len; i++) {
            c = template.charAt(i);
            if (prev == '$') {
                if (c == '$') {
                    sb = sb + c;
                    prev = 0;
                } else {
                    var number = "";
                    while (c >= '0' && c <= '9') {
                        number += c;
                        i++;
                        if (i < len)
                            c = template.charAt(i);
                        else
                            c = String.fromCharCode(0);
                    }
                    i--;
                    if (number.length > 0) {
                        number = number | 0;
                        if (subs[number] instanceof UniversalAST || subs[number] instanceof UniversalASTWithHole) {
                            if (sb.length > 0) {
                                stack.push(sb);
                                sb = "";
                            }
                            stack.push(subs[number]);
                        } else if (subs[number] !== undefined) {
                            sb = sb + subs[number];
                        } else {
                            throw new Error("Capture at index " + (number + 1) + " not found in " + subs);
                        }
                        prev = 0;
                    } else {
                        throw new Error("Expecting number or $ after $ in replacement string \"" + template + "\"");
                    }
                }
            } else if (c == '$') {
                prev = c;
            } else {
                sb = sb + c;
                prev = 0;
            }
        }
        if (sb.length > 0) {
            stack.push(sb);
            sb = "";
        }

    };


    UniversalAST.prototype.replace = function (subs) {
        var stack = [];
        var count = 0;
        for (var i = 0; i < this.children.length; i++) {
            var o = this.children[i];
            if (o instanceof UniversalAST) {
                stack.push(o.replace(subs));
            } else {
                this.replaceInString(o, subs, stack);
            }
        }
        for (var j = stack.length - 1; j >= 0; j--) {
            if (stack[j] instanceof UniversalASTWithHole) {
                var hole = stack[j];
                if (j === stack.length - 1) {
                    throw new Error("No more serialized tree left to fill a hole.");
                }
                if (!(stack[j + 1] instanceof UniversalAST)) {
                    throw new Error("A hole cannot be filled with " + stack[j + 1]);
                }
                if (hole.parent) {
                    stack[j] = hole.top;
                    hole.parent.children[hole.i] = stack[j + 1];
                } else {
                    stack[j] = stack[j + 1];
                }
                stack[j + 1] = undefined;
            } else {
                count++;
            }
        }
        var stack2 = new Array(count);
        i = 0;
        for (j = 0; j < stack.length; j++) {
            if (stack[j] !== undefined) {
                stack2[i] = stack[j];
                i++;
            }
        }
        var tmp = new UniversalAST();
        tmp.children = stack2;
        return tmp;
    };

    var cntr = UniversalAST.LB = -2;
    --cntr;
    UniversalAST.RB = --cntr;
    --cntr;
    UniversalAST.FS = --cntr;
    --cntr;

    UniversalAST.LBS = "(%";
    UniversalAST.RBS = "%)";
    UniversalAST.FSS = "/";

    UniversalAST.sTreeScanner = new StringTokenScanner(UniversalAST.FS, UniversalAST.FSS);
    UniversalAST.sTreeScanner.addString(UniversalAST.LB, UniversalAST.LBS);
    UniversalAST.sTreeScanner.addString(UniversalAST.RB, UniversalAST.RBS);

    UniversalAST.parseSTree = function (source) {
        var current;
        var root = current = new UniversalAST();
        var sb = "";
        var stack = [];

        function addString() {
            if (sb !== "") {
                current.addChild(sb);
                sb = "";
            }
        }

        UniversalAST.sTreeScanner.setStream(source);
        var token = UniversalAST.sTreeScanner.nextToken();
        while (token !== StringTokenScanner.EOF) {
            if (token === UniversalAST.LB) {
                addString();
                stack.push(current);
                current = new UniversalAST();
            } else if (token === UniversalAST.RB) {
                if (stack.length === 0) {
                    throw new Error("Unbalanced %) in " + UniversalAST.sTreeScanner.scannedPrefix());
                }
                addString();
                var prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else {
                sb = sb + UniversalAST.sTreeScanner.lexeme;
            }
            token = UniversalAST.sTreeScanner.nextToken();
        }
        addString();
        if (root !== current) {
            throw new Error("Unbalanced (% in " + source);
        }
        return root;
    };

    var universalASTEscaper = new StringTokenScanner();
    universalASTEscaper.addString(UniversalAST.LB, UniversalAST.LBS, UniversalAST.FSS + UniversalAST.LBS);
    universalASTEscaper.addString(UniversalAST.RB, UniversalAST.RBS, UniversalAST.FSS + UniversalAST.RBS);
    universalASTEscaper.addString(UniversalAST.FS, UniversalAST.FSS, UniversalAST.FSS + UniversalAST.FSS);

    function universalASTEscape(str) {
        universalASTEscaper.setStream(str);
        var token = universalASTEscaper.nextToken();
        var sb = "";
        while (token !== StringTokenScanner.EOF) {
            sb = sb + universalASTEscaper.lexeme;
            token = universalASTEscaper.nextToken();
        }
        return sb;
    }


    function TRegexAST(isContext, isAt, isStar, isAlternation) {
        if (!isAt)
            TRegexAST.parent.constructor.apply(this, arguments);
        this.isContext = isContext;
        this.isAt = isAt;
        this.isStar = isStar;
        this.isAlternation = isAlternation;
    }

    TRegexAST.LT = --cntr;
    --cntr;
    TRegexAST.RT = --cntr;
    --cntr;
    TRegexAST.LL = --cntr;
    --cntr;
    TRegexAST.RL = --cntr;
    --cntr;
    TRegexAST.LA = --cntr;
    --cntr;
    TRegexAST.RA = --cntr;
    --cntr;
    TRegexAST.AT = --cntr;
    --cntr;

    TRegexAST.LTS = "(*";
    TRegexAST.RTS = "*)";
    TRegexAST.LLS = "(**";
    TRegexAST.RLS = "**)";
    TRegexAST.LAS = "(|";
    TRegexAST.RAS = "|)";
    TRegexAST.ATS = "@";

    extend(TRegexAST, UniversalAST);


    TRegexAST.tregexScanner = new StringTokenScanner(UniversalAST.FS, UniversalAST.FSS);
    TRegexAST.tregexScanner.addString(UniversalAST.LB, UniversalAST.LBS);
    TRegexAST.tregexScanner.addString(UniversalAST.RB, UniversalAST.RBS);
    TRegexAST.tregexScanner.addString(TRegexAST.LT, TRegexAST.LTS);
    TRegexAST.tregexScanner.addString(TRegexAST.RT, TRegexAST.RTS);
    TRegexAST.tregexScanner.addString(TRegexAST.LL, TRegexAST.LLS);
    TRegexAST.tregexScanner.addString(TRegexAST.RL, TRegexAST.RLS);
    TRegexAST.tregexScanner.addString(TRegexAST.LA, TRegexAST.LAS);
    TRegexAST.tregexScanner.addString(TRegexAST.RA, TRegexAST.RAS);
    TRegexAST.tregexScanner.addString(TRegexAST.AT, TRegexAST.ATS);


    TRegexAST.ATNode = new TRegexAST(false, true);

    TRegexAST.parseTRegex = function (source, isRegex) {
        var current;
        var root = current = new TRegexAST(false);
        var sb = "";
        var stack = [];
        var prev;

        function pushString() {
            if (sb !== "") {
                current.addChild(isRegex ? new RegExp('^' + sb + '$') : sb);
                sb = "";
            }
        }

        TRegexAST.tregexScanner.setStream(source);
        var token = TRegexAST.tregexScanner.nextToken();
        while (token !== StringTokenScanner.EOF) {
            if (token === UniversalAST.LB) {
                pushString();
                stack.push(current);
                current = new TRegexAST(false);
            } else if (token === UniversalAST.RB) {
                if (stack.length === 0 || current.isContext) {
                    throw new Error("Unbalanced "+UniversalAST.RBS+" after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString();
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token === TRegexAST.LT) {
                pushString();
                stack.push(current);
                current = new TRegexAST(true);
            } else if (token === TRegexAST.RT) {
                if (stack.length === 0 || !current.isContext) {
                    throw new Error("Unbalanced "+TRegexAST.RTS+" after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString();
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token === TRegexAST.LL) {
                pushString();
                stack.push(current);
                current = new TRegexAST(false, false, true);
            } else if (token === TRegexAST.RL) {
                if (stack.length === 0 || !current.isStar) {
                    throw new Error("Unbalanced "+TRegexAST.RLS+" after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString();
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token === TRegexAST.LA) {
                pushString();
                stack.push(current);
                current = new TRegexAST(false, false, false, true);
            } else if (token === TRegexAST.RA) {
                if (stack.length === 0 || !current.isAlternation) {
                    throw new Error("Unbalanced "+TRegexAST.RAS+" after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString();
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token === TRegexAST.AT) {
                pushString();
                current.addChild(TRegexAST.ATNode);
            } else {
                sb = sb + TRegexAST.tregexScanner.lexeme;
            }
            token = TRegexAST.tregexScanner.nextToken();
        }
        pushString();
        if (root !== current) {
            throw new Error("Unbalanced (% in " + source);
        }
        return root;
    };

    TRegexAST.prototype.toString = function () {
        var ret = "";
        for (var i = 0; i < this.children.length; i++) {
            var child = this.children[i];
            if (child instanceof TRegexAST && !child.isAt) {
                ret = ret + (child.isContext ? TRegexAST.LTS : UniversalAST.LBS);
                ret = ret + child.toString();
                ret = ret + (child.isContext ? TRegexAST.RTS : UniversalAST.RBS);
            } else if (child instanceof TRegexAST && child.isAt) {
                ret = ret + "@";
            } else {
                ret = ret + tregexASTEscape(child + "");
            }
        }
        return ret;
    };

    var tregexASTEscaper = new StringTokenScanner();
    tregexASTEscaper.addString(UniversalAST.LB, UniversalAST.LBS, UniversalAST.FSS + UniversalAST.LBS);
    tregexASTEscaper.addString(UniversalAST.RB, UniversalAST.RBS, UniversalAST.FSS + UniversalAST.RBS);
    tregexASTEscaper.addString(UniversalAST.FS, UniversalAST.FSS, UniversalAST.FSS + UniversalAST.FSS);
    tregexASTEscaper.addString(TRegexAST.LT, TRegexAST.LTS, UniversalAST.FSS + TRegexAST.LTS);
    tregexASTEscaper.addString(TRegexAST.RT, TRegexAST.RTS, UniversalAST.FSS + TRegexAST.RTS);
    tregexASTEscaper.addString(TRegexAST.LL, TRegexAST.LLS, UniversalAST.FSS + TRegexAST.LLS);
    tregexASTEscaper.addString(TRegexAST.RL, TRegexAST.RLS, UniversalAST.FSS + TRegexAST.RLS);
    tregexASTEscaper.addString(TRegexAST.LA, TRegexAST.LAS, UniversalAST.FSS + TRegexAST.LAS);
    tregexASTEscaper.addString(TRegexAST.RA, TRegexAST.RAS, UniversalAST.FSS + TRegexAST.RAS);
    tregexASTEscaper.addString(TRegexAST.AT, TRegexAST.ATS, UniversalAST.FSS + TRegexAST.ATS);

    function tregexASTEscape(str) {
        tregexASTEscaper.setStream(str);
        var token = tregexASTEscaper.nextToken();
        var sb = "";
        while (token !== StringTokenScanner.EOF) {
            sb = sb + tregexASTEscaper.lexeme;
            token = tregexASTEscaper.nextToken();
        }
        return sb;
    }

    function ATransformer(predicate, pattern, modifier, replacer, isRegex) {
        this.predicate = predicate;
        this.pattern = TRegexAST.parseTRegex(pattern, isRegex);
        this.modifier = modifier;
        this.replacer = (typeof replacer === 'string') ? UniversalAST.parseSTree(replacer) : replacer;
    }

    function Transformer() {
        this.preTransformers = [];
        this.postTransformers = [];
    }

    Transformer.prototype.addTransformer = function (predicate, pattern, modifier, replacer, isRegex, isPre) {
        if (isPre) {
            this.preTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex));
        } else {
            this.postTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex));
        }
    }

    Transformer.prototype.modify = function (source, state, args) {
        if (!(source instanceof UniversalAST)) {
            source = UniversalAST.parseSTree(source);
        }
        if (typeof state !== 'object') {
            state = {};
        }
        if (typeof args !== 'object') {
            args = {};
        }
        var ret = {};
        for (var i = 0; i < this.preTransformers.length; i++) {
            var t = this.preTransformers[i];
            if (typeof t.predicate !== 'function' || t.predicate(state, args)) {
                var matches = source.matches(t.pattern);
                if (typeof t.modifier === 'function') {
                    matches = t.modifier(matches, state, args, ret);
                }
                if (t.replacer instanceof UniversalAST && matches != null) {
                    source = t.replacer.replace(matches);
                }
            }
        }
        var len = source.children.length;
        for (i = 0; i < len; i++) {
            if (source.children[i] instanceof UniversalAST)
                source.children[i] = this.modify(source.children[i], state, ret);
        }
        for (i = 0; i < this.postTransformers.length; i++) {
            t = this.postTransformers[i];
            if (typeof t.predicate !== 'function' || t.predicate(state, args)) {
                matches = source.matches(t.pattern);
                if (typeof t.modifier === 'function')
                    matches = t.modifier(matches, state, args, ret);
                if (t.replacer instanceof UniversalAST && matches != null) {
                    source = t.replacer.replace(matches);
                }
            }
        }
        return source;

    }

    function TRegexLib(pattern, isRegex, replacement) {
        this.tRegexAST = TRegexAST.parseTRegex(pattern, isRegex);
        if (replacement) this.mod = UniversalAST.parseSTree(replacement);
    }

    TRegexLib.prototype.matches = function (source) {
        var stree = TRegexLib.STree.parseSTree(source);
        return stree.matches(this.tRegexAST);
    }

    TRegexLib.prototype.replace = function (source) {
        var stree = TRegexLib.STree.parseSTree(source);
        var matches = stree.matches(this.tRegexAST);
        var mod = this.mod.replace(matches);
        return mod;
    }

    TRegexLib.STree = UniversalAST;
    TRegexLib.TRegex = TRegexAST;
    TRegexLib.Transformer = Transformer;
    TRegexLib.escapeString = universalASTEscape;

    return TRegexLib;
})
();
