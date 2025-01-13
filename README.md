# RecipeGPT 🥩🥗🧁
An Android app used for generating recipes by sending queries to OpenAI's GPT-4o model to process them and return recipes 🤤🍰


## Usage

- The app allows you to save recipes on your phone, and also it allows you note what ingredients you have around or you're going to buy. This data is stored in a sort of ***inventory*** on the phone.

- All you have to do is open the app, search the type of food you want to cook like ***chocolate cake*** or ***caeser salad***. The app will display some results and you can click on the ***details*** button to open up a new page where you'll find the exact ingredients necessary and the steps for preparing the food.

- In the recipe details page, you will see that the quantities are mentioned next to them, if the number is in red that means that you do not have enough of that specific ingredient, while green means that you have enough. You will also be able to save the recipe on your phone so you can come back to it later or delete it if you so desire. You can also click on the ***cooked*** button which basically removes from your ***inventory*** the ingredients required by that recipe.

- From the recipe details page, you can also choose to set a recipe as ***listed***. This means that the app will add to the shopping list all the necessary ingredients and the quantities required by the recipe that you still don't have in your virtual ***inventory***.

- You can view the list of ingredients you need to buy, in order to be able to prepare all the recipes you have saved on your phone, and you can select the amount and quantity that you're buying or that you already have lying around.

- You can check your ***inventory*** by navigating to the ***ingredients*** page. There you can choose to edit or delete any of the saved ingredients.

- In preferences you have 2 settings, one which sets the frequency at which you receive random cooking quotes from the internet, the other one sets the number of recipes you want to generate on each request. The waiting time can be pretty big sometimes when generating recipes, so going for large numbers isn't going to work very well, since the server might throw a timeout exception.


## Technical Details
