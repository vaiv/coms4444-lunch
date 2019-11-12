var monkey_imgs = [];
var geese_imgs = [];
var family_imgs = [];
var food_imgs = [];
var bg_img;
var monkeys;
var geese;
var family;
var food;
var first = true;

var sandwich_imgs = [];
var eaten_sandwich_imgs = [];

var fruit_imgs = [];
var eaten_fruit_imgs = [];

var egg_imgs = [];
var eaten_egg_imgs = [];

var cookie_imgs = [];
var eaten_cookie_imgs = [];

var bag_imgs = [];



function init(monkeys,geese,family,food) {

    bg_img = new Image();
    bg_img.src = 'background3.png';

    for (var i = 0; i < monkeys.length; i++) 
    {
        var monkey = monkeys[i];
        var monkey_img = new Image();
        monkey_img.src = 'monkey.png';
        monkey_imgs.push(monkey_img);
    }

    for (var i = 0; i < geese.length; i++) 
    {
        var goose = geese[i];
        var goose_img = new Image();
        goose_img.src = 'goose.png';
        geese_imgs.push(goose_img);
    }

    for (var i = 0; i < family.length; i++) 
    {
        var member = family[i];
        var member_img = new Image();
        if(i%4==0)
            member_img.src = 'jetson_1.png';
        else if (i%4==1)
            member_img.src = 'jetson_2.png';
        else if(i%4==2)
            member_img.src = 'jetson_3.png';
        else
            member_img.src = 'jetson_4.png';
        family_imgs.push(member_img);
    }

    for(var i=0;i<50;i++)
    {
        var sandwich_img = new Image();
        var eaten_sandwich_img = new Image();
        var fruit_img = new Image();
        var eaten_fruit_img = new Image();
        var egg_img = new Image();
        var eaten_egg_img = new Image();
        var cookie_img = new Image();
        var eaten_cookie_img = new Image();
        var bag_img = new Image();

        sandwich_img.src = 'sandwich.png';
        eaten_sandwich_img.src = 'sandwich.png';
        egg_img.src = 'egg.png';
        eaten_egg_img.src = 'egg_half.png';
        fruit_img.src ='orange_full.png';
        eaten_fruit_img.src = 'orange_half.png';
        cookie_img.src = 'cookie.png';
        eaten_cookie_img.src = 'eaten_cookie.png';
        bag_img.src ='bag.png';

        sandwich_imgs.push(sandwich_img);
        eaten_sandwich_imgs.push(eaten_sandwich_img);
        egg_imgs.push(egg_img);
        eaten_egg_imgs.push(eaten_egg_img);
        fruit_imgs.push(fruit_img);
        eaten_fruit_imgs.push(eaten_fruit_img);
        cookie_imgs.push(cookie_img);
        eaten_cookie_imgs.push(eaten_cookie_img);
        bag_imgs.push(bag_img);
    }
  
  window.requestAnimationFrame(draw);
}


function draw()
{
    canvas = document.getElementById('canvas');
    ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    drawBorder(ctx);

    for (var i = 0; i < monkeys.length; i++) {
        var monkey = monkeys[i];
        var drawX = ((monkey.x + 50) /100) * 1200;
        var drawY = ((monkey.y + 50) /100) * 1200;
        var aspect_ratio = 1.248;
        ctx.drawImage(monkey_imgs[i],drawX,drawY,60, 60/aspect_ratio);
        
    }


    for (var i = 0; i < geese.length; i++) {
        var goose = geese[i];
        var drawX = ((goose.x + 50) /100) * 1200;
        var drawY = ((goose.y + 50) /100) * 1200;
        var aspect_ratio = 1.659;
        ctx.drawImage(geese_imgs[i],drawX,drawY,60, 60/aspect_ratio);
      }


    for (var i = 0; i < family.length; i++) {
        var member = family[i];
        var drawX = ((member.x + 50) /100) * 1200;
        var drawY = ((member.y + 50) /100) * 1200;
        var aspect_ratio = 0.515;
        var img_size = 50;

        if((i%4)+1==3)
            {
                aspect_ratio = 0.879;
                img_size = 80;
            }
        else if ((i%4)+1==4)
            {
                aspect_ratio = 1.132;
                img_size = 80;
            }

        ctx.drawImage(family_imgs[i],drawX,drawY,img_size, img_size/aspect_ratio);
       
    }

    for(var i=0;i<food.length;i++)
    {
        var item = food[i];
        var drawX = ((item.x + 50) /100) * 1200;
        var drawY = ((item.y + 50) /100) * 1200;
        var id = parseInt(item.id);
        var iterator = new Array(9).fill(0);

        if(drawX<0 && drawY<0)
            continue;
        switch(id)
        {
            case 1: ctx.drawImage(sandwich_imgs[iterator[0]],drawX,drawY,20, sandwich_imgs[iterator[0]].height*20/sandwich_imgs[iterator[0]].width); iterator[0]++; break;
            case 2: ctx.drawImage(eaten_sandwich_imgs[iterator[1]],drawX,drawY,20, eaten_sandwich_imgs[iterator[1]].height*20/eaten_sandwich_imgs[iterator[1]].width);iterator[1]++; break;
            case 3: ctx.drawImage(fruit_imgs[iterator[2]],drawX,drawY,20, fruit_imgs[iterator[2]].height*20/fruit_imgs[iterator[2]].width);iterator[2]++; break;
            case 4: ctx.drawImage(eaten_fruit_imgs[iterator[3]],drawX,drawY,20, eaten_fruit_imgs[iterator[3]].height*20/eaten_fruit_imgs[iterator[3]].width); iterator[3]++; break;
            case 5: ctx.drawImage(egg_imgs[iterator[4]],drawX,drawY,20, egg_imgs[iterator[4]].height*20/egg_imgs[iterator[4]].width); iterator[4]++; break;
            case 6: ctx.drawImage(eaten_egg_imgs[iterator[5]],drawX,drawY,20, eaten_egg_imgs[iterator[5]].height*20/eaten_egg_imgs[iterator[5]].width); iterator[5]++; break;
            case 7: ctx.drawImage(cookie_imgs[iterator[6]],drawX,drawY,20, cookie_imgs[iterator[6]].height*20/cookie_imgs[iterator[6]].width); iterator[6]++; break;
            case 8: ctx.drawImage(eaten_cookie_imgs[iterator[7]],drawX,drawY,20, eaten_cookie_imgs[iterator[7]].height*20/eaten_cookie_imgs[iterator[7]].width); iterator[7]++; break;
            case 9: ctx.drawImage(bag_imgs[iterator[8]],drawX,drawY,40, bag_imgs[iterator[8]].height*40/bag_imgs[iterator[8]].width);iterator[8]++; break;
        }

    }

      window.requestAnimationFrame(draw);



}


function drawBorder(ctx)
{
    ctx.beginPath();
    ctx.lineWidth="4";
    ctx.strokeStyle="black";
    ctx.fillStyle="white";
    ctx.rect(0,0,1250,1250);
    ctx.drawImage(bg_img,0,0,1250,1250);
    ctx.stroke();
}

function drawMonkeys(ctx, monkeys) {
    for (var i = 0; i < monkeys.length; i++) {
        var monkey = monkeys[i];
        var img=new Image();
        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((monkey.x + 50) /100) * 1200;
            var drawY = ((monkey.y + 50) /100) * 1200;
            var aspect_ratio = 1.248;
            ctx.drawImage(img,drawX,drawY,80, 80/aspect_ratio);
            }, false);
        img.src='monkey.png';
        //console.log(bale);
        
    }
}

function drawGeese(ctx, geese) {
    for (var i = 0; i < geese.length; i++) {
        var goose = geese[i];
        var img=new Image();
        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((goose.x + 50) /100) * 1200;
            var drawY = ((goose.y + 50) /100) * 1200;
            var aspect_ratio = 1.659;
            ctx.drawImage(img,drawX,drawY,100, 100/aspect_ratio);
        }, false);
        img.src='goose.png';

        //console.log(bale);
        
    }
}

function drawFamily(ctx, family) {
    for (var i = 0; i < family.length; i++) {
        var member = family[i];
        var img=new Image();

        img.addEventListener('load', function() {
            // execute drawImage statements here
            var drawX = ((member.x + 50) /100) * 1200;
            var drawY = ((member.y + 50) /100) * 1200;
            var aspect_ratio = 0.515;

            if((i%4)+1==3)
                aspect_ratio = 0.879;
            else if ((i%4)+1==4)
                aspect_ratio = 1.132;

            ctx.drawImage(img,drawX,drawY,100, 100/aspect_ratio);

             }, false);
        img.src='jetson_'+((i%4)+1).toString()+'.png';
        //console.log(bale);
       
    }
}

function drawLine(ctx, x_start, y_start, x_end, y_end) {
    ctx.beginPath();
    ctx.moveTo(x_start, y_start);
    ctx.strokeStyle="black";
    ctx.lineTo(x_end, y_end);
    ctx.stroke();
}

var y_pos = 40;

function process(data) {
    var result = JSON.parse(data)

    // console.log(result);
    var refresh = parseFloat(result.refresh);
    monkeys = result.monkey_locations;
    geese = result.geese_locations;
    family = result.player_locations;
    food = result.food_locations;
    var player1 = result.player1;
    var player2 = result.player2;
    // console.log(result.food_locations);

    if(first)
    {
        init(monkeys,geese,family,food);
        first = false;
    }


    return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
    // console.log("Version " + version);
    var xhttp = new XMLHttpRequest();
    monkey_img = new Image();
    monkey_img.src = 'monkey.png';
    goose_img = new Image();
    goose_img.src = 'goose.png';
    xhttp.onload = (function() {
            var refresh = -1;
            try {
                if (xhttp.readyState != 4)
                    throw "Incomplete HTTP request: " + xhttp.readyState;
                if (xhttp.status != 200)
                    throw "Invalid HTTP status: " + xhttp.status;
                //console.log(xhttp.responseText);
                refresh = process(xhttp.responseText);
                if (latest_version < version)
                    latest_version = version;
                else refresh = -1;
            } catch (message) {
                alert(message);
            }

            // console.log(refresh);
            if (refresh >= 0)
                setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
        });
    xhttp.onabort = (function() { location.reload(true); });
    xhttp.onerror = (function() { location.reload(true); });
    xhttp.ontimeout = (function() {
            if (version <= latest_version)
                console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
            else if (retries == 0)
                location.reload(true);
            else {
                console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
                ajax(version, retries - 1, timeout * 2);
            }
        });
    xhttp.open("GET", "data.txt", true);
    xhttp.responseType = "text";
    xhttp.timeout = timeout;
    xhttp.send();
}

ajax(1, 10, 100);

// process('{"refresh":0, "grp_a":"g1", "grp_b":"g2", "grp_a_round":"1,2,3,4,5", "grp_b_round":"4,5,6,7,8",' +
//    '"grp_a_skills":"4,3,2,5,6,7,3", "grp_b_skills":"2,3,5,7,5,4,3", "grp_a_dist":"1,2;4,3;6,7", "grp_b_dist":"3,4;5,1;8,7", ' +
//    '"grp_a_score":"3", "grp_b_score":"0"}');
